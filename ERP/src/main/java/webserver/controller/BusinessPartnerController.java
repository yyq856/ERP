package webserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import webserver.pojo.*;
import webserver.service.BusinessPartnerService;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class BusinessPartnerController {

    @Autowired
    private BusinessPartnerService businessPartnerService;

    /**
     * 业务伙伴查询接口
     * @param request 查询请求
     * @return 查询结果
     */
    @PostMapping("/api/app/bp-search")
    public BpResponse searchBusinessPartner(@RequestBody BpSearchRequest request) {
        log.info("业务伙伴查询请求: {}", request.getCustomerId());
        return businessPartnerService.searchBusinessPartner(request);
    }

    /**
     * 创建个人业务伙伴接口
     * @param request 创建请求
     * @return 创建结果
     */
    @PostMapping("/api/app/bp-create")
    public BpResponse createBusinessPartner(@RequestBody BpCreateRequest request) {
        log.info("创建个人业务伙伴请求");
        return businessPartnerService.createBusinessPartner(request);
    }

    /**
     * 创建组业务伙伴接口
     * @param request 创建请求
     * @return 创建结果
     */
    @PostMapping("/api/app/group/create")
    public BpResponse createGroupBusinessPartner(@RequestBody GroupCreateRequest request) {
        log.info("创建组业务伙伴请求");
        return businessPartnerService.createGroupBusinessPartner(request);
    }

    // ==================== BP维护页面接口 ====================

    /**
     * BP维护页面 - 业务伙伴搜索接口
     * @param request 搜索请求
     * @return 搜索结果
     */
    @PostMapping("/api/bp/search")
    public BpMaintainSearchResponse searchBusinessPartnersForMaintain(@RequestBody BpMaintainSearchRequest request) {
        log.info("BP维护页面搜索请求: {}", request.getQuery() != null ? request.getQuery().getCustomerId() : "null");
        return businessPartnerService.searchBusinessPartnersForMaintain(request);
    }

    /**
     * BP维护页面 - 获取业务伙伴详情接口
     * @param customerId 业务伙伴ID
     * @return 详情数据
     */
    @GetMapping("/api/bp/get/{customerId}")
    public BpMaintainDetailResponse getBusinessPartnerDetail(@PathVariable String customerId) {
        log.info("获取业务伙伴详情: {}", customerId);
        return businessPartnerService.getBusinessPartnerDetail(customerId);
    }

    /**
     * BP维护页面 - 创建/编辑业务伙伴接口
     * @param request 编辑请求
     * @return 操作结果
     */
    @PostMapping("/api/bp/edit")
    public BpMaintainEditResponse editBusinessPartner(@RequestBody BpMaintainEditRequest request) {
        try {
            log.info("编辑业务伙伴请求开始");
            log.info("请求参数: {}", request);

            BpMaintainEditResponse response = businessPartnerService.editBusinessPartner(request);

            log.info("编辑业务伙伴请求完成: {}", response);
            return response;
        } catch (Exception e) {
            log.error("编辑业务伙伴请求异常: ", e);
            BpMaintainEditResponse errorResponse = new BpMaintainEditResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Internal server error: " + e.getMessage());
            return errorResponse;
        }
    }

    
    

    /**
     * 测试接口 - 验证基本功能
     */
    @GetMapping("/api/bp/test")
    public BpMaintainEditResponse testEndpoint() {
        try {
            log.info("测试接口被调用");
            BpMaintainEditResponse response = new BpMaintainEditResponse();
            response.setSuccess(true);
            response.setMessage("测试接口正常工作");

            BpMaintainEditResponse.BpEditData data = new BpMaintainEditResponse.BpEditData();
            data.setCustomerId("test");
            response.setData(data);

            return response;
        } catch (Exception e) {
            log.error("测试接口异常: ", e);
            BpMaintainEditResponse errorResponse = new BpMaintainEditResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Test error: " + e.getMessage());
            return errorResponse;
        }
    }
}
