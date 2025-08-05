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
}
