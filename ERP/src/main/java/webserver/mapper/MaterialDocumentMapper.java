package webserver.mapper;

import org.apache.ibatis.annotations.*;
import webserver.pojo.MaterialDocument;
import webserver.pojo.MaterialDocumentSearchRequest;
import webserver.pojo.MaterialDocumentSearchResponse.MaterialDocumentSummary;
import java.util.List;
import java.util.Map;

@Mapper
public interface MaterialDocumentMapper {
    
    /**
     * 搜索物料凭证
     * @param request 搜索条件
     * @return 物料凭证概览列表
     */
    List<MaterialDocumentSummary> searchMaterialDocuments(MaterialDocumentSearchRequest request);
    
    /**
     * 根据ID查询物料凭证详情
     * @param materialDocumentId 物料凭证ID
     * @return 物料凭证详情
     */
    MaterialDocument getMaterialDocumentById(@Param("materialDocumentId") Long materialDocumentId);

        /**
         * 根据业务凭证号查询其主键ID
         * @param materialDocument 业务凭证号（如 MD001）
         * @return 主键ID，未找到返回 null
         */
        @Select("SELECT material_document_id FROM erp_material_document WHERE material_document = #{materialDocument} LIMIT 1")
        Long findIdByDocumentCode(@Param("materialDocument") String materialDocument);
    
    /**
     * 查询物料凭证项目
     * @param materialDocumentId 物料凭证ID
     * @return 项目列表
     */
    @Select("SELECT mdi.material_document_id, mdi.item_no, mdi.mat_id, m.mat_desc as materialDesc, " +
            "mdi.quantity, mdi.unit, mdi.movement_type as movementType, mdi.storage_loc as storageLoc " +
            "FROM erp_material_document_item mdi " +
            "LEFT JOIN erp_material m ON mdi.mat_id = m.mat_id " +
            "WHERE mdi.material_document_id = #{materialDocumentId} " +
            "ORDER BY mdi.item_no")
    List<webserver.pojo.MaterialDocumentItem> getMaterialDocumentItems(@Param("materialDocumentId") Long materialDocumentId);
    
    /**
     * 查询物料凭证业务流程关联
     * @param materialDocumentId 物料凭证ID
     * @return 业务流程关联信息
     */
    @Select("SELECT material_document_id, dlv_id, bill_id, so_id " +
            "FROM erp_material_document_process " +
            "WHERE material_document_id = #{materialDocumentId}")
    webserver.pojo.MaterialDocumentProcess getMaterialDocumentProcess(@Param("materialDocumentId") Long materialDocumentId);

    /**
     * 根据交货单ID获取交货单信息用于生成物料凭证
     * @param deliveryId 交货单ID
     * @return 交货单信息
     */
    @Select("SELECT od.dlv_id, od.so_id, od.planned_gi_date, od.actual_gi_date, " +
            "1000 as plant_id, 'Main Plant' as plant_name " +
            "FROM erp_outbound_delivery od " +
            "WHERE od.dlv_id = #{deliveryId}")
    Map<String, Object> getDeliveryInfoForMaterialDocument(@Param("deliveryId") String deliveryId);

    /**
     * 根据交货单ID获取交货单项目信息
     * @param deliveryId 交货单ID
     * @return 交货单项目列表
     */
    @Select("SELECT oi.item_no, ei.mat_id, oi.picking_quantity as quantity, " +
            "ei.order_quantity_unit as unit, oi.storage_loc as storage_location " +
            "FROM erp_outbound_item oi " +
            "LEFT JOIN erp_item ei ON oi.ref_document_id = ei.document_id " +
            "AND oi.ref_document_type = ei.document_type AND oi.ref_item_no = ei.item_no " +
            "WHERE oi.dlv_id = #{deliveryId} AND oi.confirmation_status = 'Posted'")
    List<Map<String, Object>> getDeliveryItemsForMaterialDocument(@Param("deliveryId") String deliveryId);

    /**
     * 插入物料凭证头记录
     * @param materialDocument 物料凭证信息
     * @return 插入的记录数
     */
    @Insert("INSERT INTO erp_material_document " +
            "(material_document, material_document_year, plant_id, posting_date, document_date, created_by) " +
            "VALUES (#{materialDocument}, #{materialDocumentYear}, #{plantId}, #{postingDate}, #{documentDate}, #{createdBy})")
    @Options(useGeneratedKeys = true, keyProperty = "materialDocumentId")
    int insertMaterialDocument(MaterialDocument materialDocument);

    /**
     * 插入物料凭证项目记录
     * @param materialDocumentId 物料凭证ID
     * @param item 项目信息
     * @return 插入的记录数
     */
    @Insert("INSERT INTO erp_material_document_item " +
            "(material_document_id, item_no, mat_id, quantity, unit, movement_type, storage_loc) " +
            "VALUES (#{materialDocumentId}, #{item.item_no}, #{item.mat_id}, #{item.quantity}, #{item.unit}, '601', #{item.storage_location})")
    int insertMaterialDocumentItem(@Param("materialDocumentId") Long materialDocumentId, @Param("item") Map<String, Object> item);

    /**
     * 插入业务流程关联记录
     * @param materialDocumentId 物料凭证ID
     * @param dlvId 交货单ID
     * @param soId 销售订单ID
     * @return 插入的记录数
     */
    @Insert("INSERT INTO erp_material_document_process " +
            "(material_document_id, dlv_id, so_id) " +
            "VALUES (#{materialDocumentId}, #{dlvId}, #{soId})")
    int insertMaterialDocumentProcess(@Param("materialDocumentId") Long materialDocumentId,
                                     @Param("dlvId") Long dlvId,
                                     @Param("soId") Long soId);

    /**
     * 更新业务流程关联的账单ID
     * @param materialDocumentId 物料凭证ID
     * @param billId 账单ID
     * @return 更新的记录数
     */
    @Update("UPDATE erp_material_document_process " +
            "SET bill_id = #{billId} " +
            "WHERE material_document_id = #{materialDocumentId}")
    int updateBillingAssociation(@Param("materialDocumentId") Long materialDocumentId, @Param("billId") Long billId);

    /**
     * 生成下一个物料凭证号（基于全局主键ID，6位数字补零）
     * @return 物料凭证号
     */
    @Select("SELECT CONCAT('MD', LPAD(COALESCE(MAX(material_document_id), 0) + 1, 6, '0')) " +
            "FROM erp_material_document")
    String generateNextMaterialDocumentNumber();

    /**
     * 根据交货单ID查找Material Document ID
     * @param deliveryId 交货单ID
     * @return Material Document ID
     */
    @Select("SELECT material_document_id " +
            "FROM erp_material_document_process " +
            "WHERE dlv_id = #{deliveryId}")
    Long findMaterialDocumentIdByDeliveryId(@Param("deliveryId") Long deliveryId);
}
