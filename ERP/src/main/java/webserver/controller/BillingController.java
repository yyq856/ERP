package webserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import webserver.pojo.*;
import webserver.service.BillingService;

import java.util.*;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/app/billing")
public class BillingController {
    
    @Autowired
    private BillingService billingService;
    
    /**
     * 开票到期清单初始化接口
     * 根据开票日期和售达方条件，返回开票凭证的模板数据结构
     */
    @PostMapping("/initialize")
    public Map<String, Object> initializeBilling(@RequestBody BillingInitializeRequest request) {
        try {
            Map<String, Object> result = billingService.initializeBilling(request);
            
            // 包装成文档要求的格式
            Map<String, Object> content = new HashMap<>();
            content.put("content", result);
            content.put("message", "Billing due list loaded successfully");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", content);
            
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to load billing due list: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 开票凭证查询接口
     * 根据开票凭证号查询现有开票凭证的详细信息
     */
    @PostMapping("/get")
    public Map<String, Object> getBilling(@RequestBody BillingGetRequest request) {
        try {
            Map<String, Object> result = billingService.getBilling(request);
            if (result == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Billing document not found");
                return response;
            }
            
            // 包装成文档要求的格式
            Map<String, Object> content = new HashMap<>();
            content.put("content", result);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", content);
            
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get billing document: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 创建/编辑开票凭证接口
     * 创建新的开票凭证或修改现有开票凭证
     */
    @PostMapping("/edit")
    public Map<String, Object> editBilling(@RequestBody BillingEditRequest request) {
        try {
            Map<String, Object> result = billingService.editBilling(request);
            
            // 获取ID用于消息
            String id = "";
            if (result != null && result.get("meta") != null) {
                Map<String, Object> meta = (Map<String, Object>) result.get("meta");
                id = (String) meta.get("id");
            }
            
            String message = id != null && !id.isEmpty() 
                ? "Billing document " + id + " updated successfully" 
                : "Billing document created successfully";
            
            // 包装成文档要求的格式
            Map<String, Object> content = new HashMap<>();
            content.put("content", result);
            content.put("message", message);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", content);
            
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to edit billing document: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * 开票凭证搜索接口
     * 根据开票凭证号、开票日期、付款方等条件搜索开票凭证列表
     */
    @PostMapping("/search")
    public Map<String, Object> searchBilling(@RequestBody BillingSearchRequest request) {
        try {
            Map<String, Object> result = billingService.searchBilling(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to search billing documents: " + e.getMessage());
            return response;
        }
    }

    /**
     * 物品批量查询
     * @param items 物品查询列表
     * @return 响应结果
     */
    @PostMapping("/items-tab-query")
    public BillingResponse itemsTabQuery(@RequestBody List<BillingItemsTabQueryRequest.ItemQuery> items) {
        log.info("物品批量查询请求，项目数: {}", items != null ? items.size() : 0);
        return billingService.itemsTabQuery(items);
    }
}
