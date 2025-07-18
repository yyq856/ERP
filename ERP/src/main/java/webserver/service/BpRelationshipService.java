package webserver.service;

import webserver.pojo.*;

public interface BpRelationshipService {
    
    /**
     * 业务伙伴关系注册接口
     * @param request 注册请求
     * @return 响应结果
     */
    BpRelationshipResponse register(BpRelationshipRegisterRequest request);
    
    /**
     * 业务伙伴关系查询接口
     * @param request 查询请求
     * @return 响应结果
     */
    BpRelationshipResponse get(BpRelationshipGetRequest request);
    
    /**
     * 业务伙伴关系编辑接口
     * @param request 编辑请求
     * @return 响应结果
     */
    BpRelationshipResponse edit(BpRelationshipEditRequest request);
}
