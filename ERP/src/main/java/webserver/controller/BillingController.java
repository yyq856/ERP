package webserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import webserver.common.Response;
import webserver.pojo.*;
import webserver.service.BillingService;

import java.util.Map;

@RestController
@RequestMapping("/api/app/billing")
public class BillingController {
    
    @Autowired
    private BillingService billingService;
    
    /**
     * 开票到期清单初始化接口
     * 根据开票日期和售达方条件，返回开票凭证的模板数据结构
     */
    @PostMapping("/initialize")
    public Response<Map<String, Object>> initializeBilling(@RequestBody BillingInitializeRequest request) {
        try {
            Map<String, Object> result = billingService.initializeBilling(request);
            return Response.success(result);
        } catch (Exception e) {
            return Response.error("Failed to load billing due list: " + e.getMessage());
        }
    }
    
    /**
     * 开票凭证查询接口
     * 根据开票凭证号查询现有开票凭证的详细信息
     */
    @PostMapping("/get")
    public Response<Map<String, Object>> getBilling(@RequestBody BillingGetRequest request) {
        try {
            Map<String, Object> result = billingService.getBilling(request);
            if (result == null) {
                return Response.error("Billing document not found");
            }
            return Response.success(result);
        } catch (Exception e) {
            return Response.error("Failed to get billing document: " + e.getMessage());
        }
    }
    
    /**
     * 创建/编辑开票凭证接口
     * 创建新的开票凭证或修改现有开票凭证
     */
    @PostMapping("/edit")
    public Response<Map<String, Object>> editBilling(@RequestBody BillingEditRequest request) {
        try {
            Map<String, Object> result = billingService.editBilling(request);
            return Response.success(result);
        } catch (Exception e) {
            return Response.error("Failed to edit billing document: " + e.getMessage());
        }
    }
    
    /**
     * 开票凭证搜索接口
     * 根据开票凭证号、开票日期、付款方等条件搜索开票凭证列表
     */
    @PostMapping("/search")
    public Response<Map<String, Object>> searchBilling(@RequestBody BillingSearchRequest request) {
        try {
            Map<String, Object> result = billingService.searchBilling(request);
            return Response.success(result);
        } catch (Exception e) {
            return Response.error("Failed to search billing documents: " + e.getMessage());
        }
    }
    
    /**
     * 开票凭证物品验证接口
     * 开票凭证物品验证服务端点
     */
    @PostMapping("/items-tab-query")
    public Response<Map<String, Object>> validateBillingItems(@RequestBody ItemValidationRequest request) {
        try {
            Map<String, Object> result = billingService.validateBillingItems(request.getItems());
            return Response.success(result);
        } catch (Exception e) {
            return Response.error("Failed to validate billing items: " + e.getMessage());
        }
    }
}
