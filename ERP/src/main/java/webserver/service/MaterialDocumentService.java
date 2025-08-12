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

    /**
     * 根据“物料凭证号或ID”查询详情（支持 MD001 这类业务号，或数值型主键ID）
     * @param materialDocument 业务号（如 MD001）或数值ID字符串（如 1）
     * @return 物料凭证详情
     */
    MaterialDocumentDetailResponse getMaterialDocumentDetail(String materialDocument);
}
