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

        if (request.getBankData() != null && request.getBankData().getAmount() != null) {
            if (request.getBankData().getAmount().getAmount() != null) {
                requestBalance = request.getBankData().getAmount().getAmount().doubleValue();
            }
            if (request.getBankData().getAmount().getUnit() != null && !request.getBankData().getAmount().getUnit().isEmpty()) {
                balanceUnit = request.getBankData().getAmount().getUnit();
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

                // 4.1 客户付款：增加客户余额（正数）
                boolean paymentUpdated = customerBalanceService.updateCustomerBalance(
                    customerId, companyCode, currency, paymentAmount);

                if (!paymentUpdated) {
                    System.err.println("更新客户付款余额失败");
                } else {
                    System.out.println(String.format("客户 %s 付款余额更新成功: +%s %s",
                        customerId, paymentAmount, currency));
                }

                // 4.2 清算账单：减少客户余额（负数）
                // 获取账单的实际金额
                BigDecimal billAmount = new BigDecimal(billInfo.get("amount").toString());
                if (billAmount.compareTo(BigDecimal.ZERO) > 0) {
                    boolean clearingUpdated = customerBalanceService.updateCustomerBalance(
                        customerId, companyCode, currency, billAmount.negate());

                    if (!clearingUpdated) {
                        System.err.println("更新客户清算余额失败");
                    } else {
                        System.out.println(String.format("客户 %s 清算余额更新成功: -%s %s",
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
                    processIncomingPayment(billId);
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

}
