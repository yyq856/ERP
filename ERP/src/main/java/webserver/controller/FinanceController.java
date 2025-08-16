package webserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import webserver.common.Response;
import webserver.pojo.SearchOpenItemsRequest;
import webserver.service.FinanceService;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/finance")
public class FinanceController {
    
    @Autowired
    private FinanceService financeService;
    
    /**
     * 查询未清算账单（开放项目）
     * @param request 查询条件
     * @return 未清算账单列表
     */
    @PostMapping("/searchOpenItems")
    public Response<List<Map<String, Object>>> searchOpenItems(@RequestBody SearchOpenItemsRequest request) {
        try {
            // 从请求体中提取accountID
            if (request.getOpenItemSelection() == null) {
                return Response.error("openItemSelection不能为空");
            }
            
            Object accountIdObj = request.getOpenItemSelection().getAccountID();
            if (accountIdObj == null) {
                return Response.error("accountID不能为空");
            }
            
            String accountId = accountIdObj.toString();
            if (accountId.isEmpty()) {
                return Response.error("accountID不能为空");
            }
            
            List<Map<String, Object>> bills = financeService.getUnclearBillsByAccountId(accountId);
            return Response.success(bills);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("查询未清算账单失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理 incoming payment，更新账单状态并创建付款记录
     * @param request 请求体
     * @return 操作结果
     */
    @PostMapping("/postOpenItems")
    public Response<Map<String, Object>> postOpenItems(@RequestBody Map<String, Object> request) {
        try {
            // 从请求体中提取account字段（账单ID）
            Object accountObj = request.get("account");
            if (accountObj == null) {
                return Response.error("account字段不能为空");
            }

            String billId = accountObj.toString();
            if (billId.isEmpty()) {
                return Response.error("account字段不能为空");
            }

            Map<String, Object> result = financeService.processIncomingPayment(billId);
            return Response.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("处理incoming payment失败: " + e.getMessage());
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
     * 调试接口：更新账单状态为UNCLEAR
     */
    @PostMapping("/debug/updateStatusToUnclear/{billId}")
    public Response<String> updateStatusToUnclear(@PathVariable String billId) {
        try {
            boolean success = financeService.updateBillStatusToUnclear(billId);
            if (success) {
                return Response.success("账单状态已更新为UNCLEAR");
            } else {
                return Response.error("更新失败，账单不存在或状态未改变");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("更新账单状态失败: " + e.getMessage());
        }
    }

}
