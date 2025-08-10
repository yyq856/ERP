package webserver.service;

import webserver.pojo.MaterialDocumentSearchRequest;
import webserver.pojo.MaterialDocumentSearchResponse;
import webserver.pojo.MaterialDocumentDetailResponse;

public interface MaterialDocumentService {
    
    /**
     * 搜索物料凭证
     * @param request 搜索条件
     * @return 搜索结果
     */
    MaterialDocumentSearchResponse searchMaterialDocuments(MaterialDocumentSearchRequest request);
    
    /**
     * 根据ID查询物料凭证详情
     * @param materialDocumentId 物料凭证ID
     * @return 物料凭证详情
     */
    MaterialDocumentDetailResponse getMaterialDocumentDetail(Long materialDocumentId);
}
