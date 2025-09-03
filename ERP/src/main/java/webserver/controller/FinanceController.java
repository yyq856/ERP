package webserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import webserver.common.Response;
import webserver.pojo.SearchOpenItemsRequest;
import webserver.service.FinanceService;
import webserver.service.CustomerBalanceService;
import webserver.service.SalesOrderCalculationService;
import webserver.service.impl.CustomerBalanceServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/finance")
public class FinanceController {

    private static final Logger logger = LoggerFactory.getLogger(FinanceController.class);

    @Autowired
    private FinanceService financeService;

    @Autowired
    private CustomerBalanceService customerBalanceService;

    @Autowired
    private SalesOrderCalculationService salesOrderCalculationService;
    
    /**
     * 查询未清算账单（开放项目）
     * @param request 查询条件
     * @return 未清算账单列表
     */
    @PostMapping("/searchOpenItems")
    public Response<Map<String, Object>> searchOpenItems(@RequestBody SearchOpenItemsRequest request) {
        try {
            // 验证请求参数
            if (request.getGeneralInformation() == null) {
                return Response.error("generalInformation不能为空");
            }

            String companyCode = request.getGeneralInformation().getCompanyCode();
            if (companyCode == null || companyCode.isEmpty()) {
                return Response.error("companyCode不能为空");
            }

            Map<String, Object> result = financeService.searchOpenItems(request);
            return Response.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("查询未清项失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理 incoming payment，按照新的数据结构处理客户付款和账单清算
     * @param requestBody 请求体，包含customerPayment和items数组
     * @return 操作结果
     */
    @PostMapping("/postOpenItems")
    public Response<Map<String, Object>> postOpenItems(@RequestBody Map<String, Object> requestBody) {
        try {
            if (requestBody == null) {
                return Response.error("请求体不能为空");
            }

            // 解析customerPayment
            Map<String, Object> customerPayment = (Map<String, Object>) requestBody.get("customerPayment");
            if (customerPayment == null) {
                return Response.error("customerPayment字段不能为空");
            }

            // 解析items数组
            List<Map<String, Object>> items = (List<Map<String, Object>>) requestBody.get("items");
            if (items == null || items.isEmpty()) {
                return Response.error("items字段不能为空");
            }

            Map<String, Object> result = financeService.processCustomerPaymentAndClearItems(customerPayment, items);
            return Response.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("处理客户付款失败: " + e.getMessage());
        }
    }

    /**
     * 调试接口：查询账单状态
     */
    @GetMapping("/debug/bills")
    public Response<List<Map<String, Object>>> debugBills() {
        try {
            List<Map<String, Object>> bills = financeService.getAllBillsForDebug();
            return Response.success(bills);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("查询账单失败: " + e.getMessage());
        }
    }

    /**
     * 调试接口：更新账单状态为UNCLEAR (现在实际更新为OPEN)
     */
    @PostMapping("/debug/updateStatusToUnclear/{billId}")
    public Response<String> updateStatusToUnclear(@PathVariable String billId) {
        try {
            boolean success = financeService.updateBillStatusToUnclear(billId);
            if (success) {
                return Response.success("账单状态已更新为OPEN");
            } else {
                return Response.error("更新失败，账单不存在或状态未改变");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("更新账单状态失败: " + e.getMessage());
        }
    }

    /**
     * 调试接口：更新账单状态为OPEN
     */
    @PostMapping("/debug/updateStatusToOpen/{billId}")
    public Response<String> updateStatusToOpen(@PathVariable String billId) {
        try {
            boolean success = financeService.updateBillStatusToOpen(billId);
            if (success) {
                return Response.success("账单状态已更新为OPEN");
            } else {
                return Response.error("更新失败，账单不存在或状态未改变");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("更新账单状态失败: " + e.getMessage());
        }
    }

    /**
     * 调试接口：获取客户余额
     */
    @GetMapping("/debug/balance/{customerId}/{companyCode}/{currency}")
    public Response<BigDecimal> getCustomerBalance(@PathVariable String customerId,
                                                 @PathVariable String companyCode,
                                                 @PathVariable String currency) {
        try {
            BigDecimal balance = customerBalanceService.getCustomerBalance(customerId, companyCode, currency);
            return Response.success(balance);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("获取客户余额失败: " + e.getMessage());
        }
    }

    /**
     * 调试接口：更新客户余额
     */
    @PostMapping("/debug/balance/{customerId}/{companyCode}/{currency}/{amount}")
    public Response<String> updateCustomerBalance(@PathVariable String customerId,
                                                @PathVariable String companyCode,
                                                @PathVariable String currency,
                                                @PathVariable BigDecimal amount) {
        try {
            boolean success = customerBalanceService.updateCustomerBalance(customerId, companyCode, currency, amount);
            if (success) {
                BigDecimal newBalance = customerBalanceService.getCustomerBalance(customerId, companyCode, currency);
                return Response.success("余额更新成功，新余额: " + newBalance);
            } else {
                return Response.error("余额更新失败，可能导致负余额");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("更新客户余额失败: " + e.getMessage());
        }
    }

    /**
     * 调试接口：获取所有客户余额
     */
    @GetMapping("/debug/balances")
    public Response<Map<String, BigDecimal>> getAllBalances() {
        try {
            if (customerBalanceService instanceof CustomerBalanceServiceImpl) {
                CustomerBalanceServiceImpl impl = (CustomerBalanceServiceImpl) customerBalanceService;
                return Response.success(impl.getAllBalances());
            } else {
                return Response.error("不支持的服务实现");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("获取所有余额失败: " + e.getMessage());
        }
    }

    /**
     * 调试接口：获取公司指定货币的总余额
     */
    @GetMapping("/debug/totalBalance/{companyCode}/{currency}")
    public Response<BigDecimal> getTotalBalanceByCompanyAndCurrency(@PathVariable String companyCode,
                                                                  @PathVariable String currency) {
        try {
            BigDecimal totalBalance = customerBalanceService.getTotalBalanceByCompanyAndCurrency(companyCode, currency);
            return Response.success(totalBalance);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("获取公司总余额失败: " + e.getMessage());
        }
    }

    /**
     * 调试接口：重新计算单个销售订单的金额
     */
    @PostMapping("/debug/recalculateSalesOrder/{soId}")
    public Response<String> recalculateSalesOrderAmounts(@PathVariable Long soId) {
        try {
            boolean success = salesOrderCalculationService.recalculateAndUpdateSalesOrderAmounts(soId);
            if (success) {
                return Response.success("销售订单 " + soId + " 金额重新计算成功");
            } else {
                return Response.error("销售订单 " + soId + " 金额重新计算失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("重新计算销售订单金额失败: " + e.getMessage());
        }
    }

    /**
     * 调试接口：批量更新所有销售订单的金额
     */
    @PostMapping("/debug/batchUpdateSalesOrders")
    public Response<String> batchUpdateAllSalesOrderAmounts() {
        try {
            int updatedCount = salesOrderCalculationService.batchUpdateAllSalesOrderAmounts();
            return Response.success("批量更新完成，成功更新 " + updatedCount + " 个销售订单");
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("批量更新销售订单金额失败: " + e.getMessage());
        }
    }

    /**
     * 调试接口：计算销售订单金额（不更新数据库）
     */
    @GetMapping("/debug/calculateSalesOrder/{soId}")
    public Response<SalesOrderCalculationService.SalesOrderAmountResult> calculateSalesOrderAmounts(@PathVariable Long soId) {
        try {
            SalesOrderCalculationService.SalesOrderAmountResult result = salesOrderCalculationService.calculateSalesOrderAmounts(soId);
            if (result != null) {
                return Response.success(result);
            } else {
                return Response.error("无法计算销售订单 " + soId + " 的金额");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("计算销售订单金额失败: " + e.getMessage());
        }
    }

    /**
     * 获取销售订单信息（用于调试验证）
     */
    @GetMapping("/debug/salesOrder/{soId}")
    public Response<Map<String, Object>> getSalesOrderInfo(@PathVariable Long soId) {
        try {
            Map<String, Object> salesOrderInfo = salesOrderCalculationService.getSalesOrderInfo(soId);
            return Response.success(salesOrderInfo);
        } catch (Exception e) {
            logger.error("获取销售订单 {} 信息时出错: ", soId, e);
            return Response.error("获取销售订单信息失败");
        }
    }

}
