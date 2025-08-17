package webserver.service;

import webserver.pojo.Item;
import java.util.List;
import java.util.Map;

/**
 * 统一的Item服务接口
 * 处理所有业务类型的item操作，统一使用erp_item表
 */
public interface UnifiedItemService {
    
    /**
     * 更新文档的所有items
     * 删除现有items，按顺序重新分配行号并插入新items
     *
     * @param documentId 文档ID
     * @param documentType 文档类型 (inquiry, quotation, sales_order, billing_doc)
     * @param frontendItems 前端传入的item数据列表
     */
    void updateDocumentItems(Long documentId, String documentType, List<Map<String, Object>> frontendItems);

    /**
     * 更新文档的所有items（不触发事件）
     * 删除现有items，按顺序重新分配行号并插入新items，但不发布事件
     *
     * @param documentId 文档ID
     * @param documentType 文档类型 (inquiry, quotation, sales_order, billing_doc)
     * @param frontendItems 前端传入的item数据列表
     */
    void updateDocumentItemsWithoutEvent(Long documentId, String documentType, List<Map<String, Object>> frontendItems);
    
    /**
     * 查询文档的所有items
     *
     * @param documentId 文档ID
     * @param documentType 文档类型
     * @return Item列表
     */
    List<Item> getDocumentItems(Long documentId, String documentType);

    /**
     * 统一读取文档items并转换为前端格式
     *
     * @param documentId 文档ID
     * @param documentType 文档类型
     * @return 前端标准item格式列表
     */
    List<Map<String, Object>> getDocumentItemsAsFrontendFormat(Long documentId, String documentType);

    /**
     * 删除文档的所有items
     *
     * @param documentId 文档ID
     * @param documentType 文档类型
     */
    void deleteDocumentItems(Long documentId, String documentType);
}
