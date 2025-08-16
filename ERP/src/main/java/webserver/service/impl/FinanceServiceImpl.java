package webserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webserver.mapper.FinanceMapper;
import webserver.pojo.SearchOpenItemsRequest;
import webserver.service.FinanceService;
import webserver.service.CustomerBalanceService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinanceServiceImpl implements FinanceService {
    
    @Autowired
    private FinanceMapper financeMapper;

    @Autowired
    private CustomerBalanceService customerBalanceService;
    
    @Override
    public Map<String, Object> searchOpenItems(SearchOpenItemsRequest request) {
        Map<String, Object> result = new HashMap<>();

        // 从请求的 bankData 获取初始余额和币种
        double requestBalance = 0;
        String balanceUnit = "EUR"; // 默认货币

        // 🔥 使用新的统一方法获取金额和货币单位
        if (request.getBankData() != null) {
            try {
                String amountStr = request.getBankData().getAmountValue();
                if (amountStr != null && !amountStr.isEmpty()) {
                    requestBalance = Double.parseDouble(amountStr);
                }
            } catch (NumberFormatException e) {
                System.err.println("解析金额失败: " + request.getBankData().getAmountValue());
                requestBalance = 0.0;
            }

            String unit = request.getBankData().getAmountUnit();
            if (unit != null && !unit.isEmpty()) {
                balanceUnit = unit;
            }
        }

        // 获取指定货币的未清项列表
        List<Map<String, Object>> openItems = financeMapper.searchOpenItems(
            request.getGeneralInformation(), balanceUnit);

        // 🔥 获取指定客户的余额（精确查找）
        double customerBalance = 0;
        if (request.getGeneralInformation() != null) {
            String customerID = request.getGeneralInformation().getCustomerID();
            String companyCode = request.getGeneralInformation().getCompanyCode();

            // 如果没有提供companyCode，使用默认值
            if (companyCode == null || companyCode.isEmpty()) {
                companyCode = "1000"; // 默认公司代码
            }

            // 如果提供了customerID，获取该客户的精确余额
            if (customerID != null && !customerID.isEmpty()) {
                try {
                    BigDecimal balance = customerBalanceService.getCustomerBalance(customerID, companyCode, balanceUnit);
                    customerBalance = balance.doubleValue();
                    System.out.println(String.format("客户 %s 在公司 %s 的 %s 货币余额: %.2f",
                        customerID, companyCode, balanceUnit, customerBalance));
                } catch (Exception e) {
                    System.err.println("获取客户余额失败: " + e.getMessage());
                    customerBalance = 0;
                }
            }
        }

        // 总余额 = 请求余额 + 指定客户的余额
        double totalBalance = requestBalance + customerBalance;

        result.put("balance", String.format("%.2f", totalBalance));
        result.put("balanceUnit", balanceUnit);
        result.put("openItems", openItems);

        return result;
    }

    @Override
    public List<Map<String, Object>> getUnclearBillsByAccountId(String accountId) {
        return financeMapper.getUnclearBillsByAccountId(accountId);
    }

    @Override
    public Map<String, Object> processIncomingPayment(String billId) {
        // 开始事务处理
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 获取账单信息用于创建付款记录
            Map<String, Object> billInfo = financeMapper.getBillInfoForPayment(billId);
            if (billInfo == null || billInfo.isEmpty()) {
                throw new RuntimeException("未找到账单信息: " + billId);
            }

            // 2. 更新账单状态为CLEAR
            int updatedRows = financeMapper.updateBillStatusToClear(billId);
            if (updatedRows <= 0) {
                throw new RuntimeException("更新账单状态失败: " + billId);
            }

            // 3. 插入付款记录
            int insertedRows = financeMapper.insertPayment(billInfo);
            if (insertedRows <= 0) {
                throw new RuntimeException("插入付款记录失败");
            }

            // 🔥 4. 更新客户余额
            try {
                String customerId = billInfo.get("customerId").toString();
                String companyCode = "1000"; // 默认公司代码，可以从请求中获取
                String currency = billInfo.get("currency").toString();
                BigDecimal amount = new BigDecimal(billInfo.get("amount").toString());
                // amount = amount.negate();

                // 付款会增加客户余额（正数）
                boolean balanceUpdated = customerBalanceService.updateCustomerBalance(
                    customerId, companyCode, currency, amount);

                if (!balanceUpdated) {
                    System.err.println("更新客户余额失败，但付款记录已创建");
                } else {
                    System.out.println(String.format("客户 %s 余额更新成功: +%s %s",
                        customerId, amount, currency));
                }
            } catch (Exception e) {
                System.err.println("更新客户余额时发生错误: " + e.getMessage());
                // 不抛出异常，因为付款记录已经创建成功
            }

            // 5. 构建返回结果 - 按前端期望的结构
            Map<String, Object> journalEntry = new HashMap<>();
            journalEntry.put("journalEntryId", billInfo.get("billId"));
            journalEntry.put("billId", billInfo.get("billId"));
            journalEntry.put("customerId", billInfo.get("customerId"));
            journalEntry.put("amount", billInfo.get("amount"));
            journalEntry.put("currency", billInfo.get("currency"));
            journalEntry.put("postingDate", billInfo.get("postingDate"));
            journalEntry.put("status", "CLEAR");

            result.put("JournalEntry", journalEntry);
            result.put("message", "付款处理成功");

            return result;
        } catch (Exception e) {
            throw new RuntimeException("处理incoming payment失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> processIncomingPaymentWithAmount(String billId, BigDecimal paymentAmount, String currency) {
        // 开始事务处理
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 获取账单信息用于创建付款记录
            Map<String, Object> billInfo = financeMapper.getBillInfoForPayment(billId);
            if (billInfo == null || billInfo.isEmpty()) {
                throw new RuntimeException("未找到账单信息: " + billId);
            }

            // 2. 更新账单状态为CLEAR
            int updatedRows = financeMapper.updateBillStatusToClear(billId);
            if (updatedRows <= 0) {
                throw new RuntimeException("更新账单状态失败: " + billId);
            }

            // 3. 插入付款记录（使用客户实际支付的金额）
            Map<String, Object> paymentInfo = new HashMap<>(billInfo);
            paymentInfo.put("amount", paymentAmount.doubleValue());
            paymentInfo.put("currency", currency);

            int insertedRows = financeMapper.insertPayment(paymentInfo);
            if (insertedRows <= 0) {
                throw new RuntimeException("插入付款记录失败");
            }

            // 🔥 4. 更新客户余额（完整的付款和清算逻辑）
            try {
                String customerId = billInfo.get("customerId").toString();
                String companyCode = "1000"; // 默认公司代码，可以从请求中获取

                // 🔥 正确的逻辑：+汇款 -账单

                // 获取账单的实际金额
                BigDecimal billAmount = new BigDecimal(billInfo.get("amount").toString());

                // 4.1 客户付款：增加客户余额（+汇款）
                boolean paymentUpdated = customerBalanceService.updateCustomerBalance(
                    customerId, companyCode, currency, paymentAmount);

                if (!paymentUpdated) {
                    System.err.println("更新客户付款余额失败");
                } else {
                    System.out.println(String.format("客户 %s 付款余额更新成功: +%s %s",
                        customerId, paymentAmount, currency));
                }

                // 4.2 清算账单：减少客户余额（-账单）
                if (billAmount.compareTo(BigDecimal.ZERO) > 0) {
                    // 🔥 直接用负数，不用negate()方法
                    BigDecimal negativeBillAmount = new BigDecimal("-" + billAmount.toString());
                    boolean clearingUpdated = customerBalanceService.updateCustomerBalance(
                        customerId, companyCode, currency, negativeBillAmount);

                    if (!clearingUpdated) {
                        System.err.println("更新客户清算余额失败");
                    } else {
                        System.out.println(String.format("客户 %s 清算余额更新成功: %s %s",
                            customerId, billAmount, currency));
                    }
                }

            } catch (Exception e) {
                System.err.println("更新客户余额时发生错误: " + e.getMessage());
                // 不抛出异常，因为付款记录已经创建成功
            }

            // 5. 构建返回结果 - 按前端期望的结构
            Map<String, Object> journalEntry = new HashMap<>();
            journalEntry.put("journalEntryId", billInfo.get("billId"));
            journalEntry.put("billId", billInfo.get("billId"));
            journalEntry.put("customerId", billInfo.get("customerId"));
            journalEntry.put("amount", paymentAmount.doubleValue());
            journalEntry.put("currency", currency);
            journalEntry.put("postingDate", billInfo.get("postingDate"));
            journalEntry.put("status", "CLEAR");

            result.put("JournalEntry", journalEntry);
            result.put("message", "付款处理成功");

            return result;
        } catch (Exception e) {
            throw new RuntimeException("处理incoming payment失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理带余额验证的付款（支持多个账单）
     */
    public Map<String, Object> processIncomingPaymentWithBalance(List<Map<String, Object>> openItems) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 计算总金额和获取客户信息
            BigDecimal totalAmount = BigDecimal.ZERO;
            String customerId = null;
            String companyCode = null;
            String currency = "USD";

            for (Map<String, Object> item : openItems) {
                if (item.get("amount") != null) {
                    BigDecimal amount = new BigDecimal(item.get("amount").toString());
                    totalAmount = totalAmount.add(amount);
                }
                if (customerId == null && item.get("account") != null) {
                    customerId = item.get("account").toString(); // 使用 account 作为客户ID
                }
                if (companyCode == null && item.get("companyCode") != null) {
                    companyCode = item.get("companyCode").toString();
                }
                if (item.get("amountUnit") != null) {
                    currency = item.get("amountUnit").toString();
                }
            }

            // 检查客户余额是否足够
            if (customerId != null && companyCode != null) {
                BigDecimal currentBalance = customerBalanceService.getCustomerBalance(customerId, companyCode, currency);

                // 如果余额不足，返回错误
                if (currentBalance.add(totalAmount).compareTo(BigDecimal.ZERO) < 0) {
                    result.put("success", false);
                    result.put("message", "客户余额不足，无法处理付款");
                    return result;
                }

                // 更新客户余额（减去付款金额）
                boolean balanceUpdated = customerBalanceService.updateCustomerBalance(
                    customerId, companyCode, currency, totalAmount.negate());

                if (!balanceUpdated) {
                    result.put("success", false);
                    result.put("message", "更新客户余额失败");
                    return result;
                }
            }

            // 处理每个账单
            for (Map<String, Object> item : openItems) {
                if (item.get("journalEntry") != null) {
                    String billId = item.get("journalEntry").toString();

                    // 🔥 获取付款金额和货币
                    BigDecimal paymentAmount = BigDecimal.ZERO;
                    String itemCurrency = "USD";

                    System.out.println("🔥 调试：处理账单 " + billId + "，item数据: " + item);

                    if (item.get("paymentAmount") != null) {
                        paymentAmount = new BigDecimal(item.get("paymentAmount").toString());
                        System.out.println("🔥 从paymentAmount获取金额: " + paymentAmount);
                    } else if (item.get("amount") != null) {
                        // 如果没有指定付款金额，使用账单金额
                        paymentAmount = new BigDecimal(item.get("amount").toString());
                        System.out.println("🔥 从amount获取金额: " + paymentAmount);
                    }

                    if (item.get("currency") != null) {
                        itemCurrency = item.get("currency").toString();
                        System.out.println("🔥 从currency获取货币: " + itemCurrency);
                    } else if (item.get("amountUnit") != null) {
                        itemCurrency = item.get("amountUnit").toString();
                        System.out.println("🔥 从amountUnit获取货币: " + itemCurrency);
                    }

                    System.out.println("🔥 调用processIncomingPaymentWithAmount: billId=" + billId + ", paymentAmount=" + paymentAmount + ", currency=" + itemCurrency);

                    // 🔥 使用新的方法，包含完整的付款和清算逻辑
                    processIncomingPaymentWithAmount(billId, paymentAmount, itemCurrency);
                }
            }

            // 构建成功响应
            Map<String, Object> journalEntry = new HashMap<>();
            journalEntry.put("journalEntryId", openItems.get(0).get("journalEntry"));
            journalEntry.put("status", "CLEAR");
            journalEntry.put("totalAmount", totalAmount);
            journalEntry.put("currency", currency);

            result.put("JournalEntry", journalEntry);
            result.put("message", "付款处理成功");

            return result;
        } catch (Exception e) {
            throw new RuntimeException("处理incoming payment失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getAllBillsForDebug() {
        return financeMapper.getAllBillsForDebug();
    }

    @Override
    public boolean updateBillStatusToUnclear(String billId) {
        try {
            int updated = financeMapper.updateBillStatusToUnclear(billId);
            return updated > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新账单状态为OPEN失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateBillStatusToOpen(String billId) {
        try {
            int updated = financeMapper.updateBillStatusToUnclear(billId); // 这个方法现在更新为OPEN
            return updated > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新账单状态为OPEN失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> processCustomerPaymentAndClearItems(Map<String, Object> customerPayment, List<Map<String, Object>> items) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 解析客户付款信息（添加空值检查）
            Object customerObj = customerPayment.get("customer");
            Object companyCodeObj = customerPayment.get("companyCode");
            Object currencyObj = customerPayment.get("currency");
            Object amountObj = customerPayment.get("amount");

            if (customerObj == null) {
                result.put("success", false);
                result.put("message", "customer字段不能为空");
                return result;
            }
            if (companyCodeObj == null) {
                result.put("success", false);
                result.put("message", "companyCode字段不能为空（注意大小写）");
                return result;
            }
            if (currencyObj == null) {
                result.put("success", false);
                result.put("message", "currency字段不能为空");
                return result;
            }
            if (amountObj == null) {
                result.put("success", false);
                result.put("message", "amount字段不能为空");
                return result;
            }

            String customerId = customerObj.toString();
            String companyCode = companyCodeObj.toString();
            String currency = currencyObj.toString();
            BigDecimal paymentAmount = new BigDecimal(amountObj.toString());

            System.out.println(String.format("🔥 处理客户付款: 客户=%s, 公司=%s, 货币=%s, 付款金额=%s",
                customerId, companyCode, currency, paymentAmount));

            // 2. 计算amountDelta = +payment - sum(items)
            BigDecimal amountDelta = paymentAmount; // 先加上付款金额

            for (Map<String, Object> item : items) {
                BigDecimal itemAmount = new BigDecimal(item.get("amount").toString());
                amountDelta = amountDelta.subtract(itemAmount); // 减去每个账单金额
                System.out.println(String.format("🔥 处理账单项: 金额=%s, 当前delta=%s", itemAmount, amountDelta));
            }

            System.out.println(String.format("🔥 最终amountDelta: %s", amountDelta));

            // 3. 获取当前余额
            BigDecimal currentBalance = customerBalanceService.getCustomerBalance(customerId, companyCode, currency);
            if (currentBalance == null) {
                currentBalance = BigDecimal.ZERO;
                System.out.println("🔥 客户余额不存在，初始化为0");
            }
            System.out.println(String.format("🔥 当前余额: %s", currentBalance));

            // 4. 计算新余额
            BigDecimal newBalance = currentBalance.add(amountDelta);
            System.out.println(String.format("🔥 新余额: %s", newBalance));

            // 5. 检查余额不能为负
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                result.put("success", false);
                result.put("message", String.format("余额不足！当前余额: %s, 需要: %s, 差额: %s",
                    currentBalance, amountDelta.negate(), newBalance));
                return result;
            }

            // 6. 直接设置新余额
            boolean success = customerBalanceService.setCustomerBalance(customerId, companyCode, currency, newBalance);

            if (success) {
                // 7. 更新账单状态为CLEAR
                int clearedBillsCount = 0;
                for (Map<String, Object> item : items) {
                    Object journalEntryObj = item.get("journalEntry");
                    if (journalEntryObj != null) {
                        String billId = journalEntryObj.toString();
                        try {
                            int updated = financeMapper.updateBillStatusToClear(billId);
                            if (updated > 0) {
                                clearedBillsCount++;
                                System.out.println(String.format("🔥 账单 %s 状态已更新为CLEAR", billId));
                            } else {
                                System.err.println(String.format("⚠️ 账单 %s 状态更新失败", billId));
                            }
                        } catch (Exception e) {
                            System.err.println(String.format("❌ 更新账单 %s 状态时发生错误: %s", billId, e.getMessage()));
                        }
                    }
                }

                result.put("success", true);
                result.put("message", "客户付款和账单清算成功");
                result.put("oldBalance", currentBalance);
                result.put("newBalance", newBalance);
                result.put("amountDelta", amountDelta);
                result.put("paymentAmount", paymentAmount);
                result.put("itemsCount", items.size());
                result.put("clearedBillsCount", clearedBillsCount);

                System.out.println(String.format("🔥 处理成功: 余额从 %s 变为 %s (变化: %s), 清算账单数: %d",
                    currentBalance, newBalance, amountDelta, clearedBillsCount));
            } else {
                result.put("success", false);
                result.put("message", "更新客户余额失败");
            }

            return result;

        } catch (Exception e) {
            System.err.println("处理客户付款和清算失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "处理失败: " + e.getMessage());
            return result;
        }
    }

}
