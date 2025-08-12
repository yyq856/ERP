package webserver.mapper;

import org.apache.ibatis.annotations.*;
import webserver.pojo.MaterialDocument;
import webserver.pojo.MaterialDocumentSearchRequest;
import webserver.pojo.MaterialDocumentSearchResponse.MaterialDocumentSummary;
import java.util.List;

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
}
