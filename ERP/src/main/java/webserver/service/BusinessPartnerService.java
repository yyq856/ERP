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

    /**
     * BP维护页面 - 搜索业务伙伴
     * @param request 搜索请求
     * @return 搜索结果
     */
    BpMaintainSearchResponse searchBusinessPartnersForMaintain(BpMaintainSearchRequest request);

    /**
     * BP维护页面 - 获取业务伙伴详情
     * @param customerId 业务伙伴ID
     * @return 详情数据
     */
    BpMaintainDetailResponse getBusinessPartnerDetail(String customerId);

    /**
     * BP维护页面 - 创建/编辑业务伙伴
     * @param request 编辑请求
     * @return 操作结果
     */
    BpMaintainEditResponse editBusinessPartner(BpMaintainEditRequest request);
}
