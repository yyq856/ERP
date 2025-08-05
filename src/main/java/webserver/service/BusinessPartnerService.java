package webserver.service;

import webserver.pojo.*;

public interface BusinessPartnerService {
    
    /**
     * 查询业务伙伴
     * @param request 查询请求
     * @return 查询结果
     */
    BpResponse searchBusinessPartner(BpSearchRequest request);
    
    /**
     * 创建个人业务伙伴
     * @param request 创建请求
     * @return 创建结果
     */
    BpResponse createBusinessPartner(BpCreateRequest request);
    
    /**
     * 创建组业务伙伴
     * @param request 创建请求
     * @return 创建结果
     */
    BpResponse createGroupBusinessPartner(GroupCreateRequest request);
}
