package webserver.mapper;

import org.apache.ibatis.annotations.*;
import webserver.pojo.Item;

import java.util.List;

/**
 * 统一项目Mapper接口
 * 支持所有业务类型的项目操作
 */
@Mapper
public interface ItemMapper {

    /**
     * 根据文档ID和类型查询项目列表
     * @param documentId 文档ID
     * @param documentType 文档类型
     * @return 项目列表
     */
    @Select("SELECT document_id as documentId, document_type as documentType, item_no as itemNo, " +
            "mat_id as matId, quantity, net_price as netPrice, item_value as itemValue, " +
            "plant_id as plantId, su, item_code as itemCode, material_code as materialCode, " +
            "order_quantity_str as orderQuantityStr, order_quantity_unit as orderQuantityUnit, " +
            "description, req_deliv_date as reqDelivDate, net_value_str as netValueStr, " +
            "net_value_unit as netValueUnit, tax_value_str as taxValueStr, tax_value_unit as taxValueUnit, " +
            "pricing_date as pricingDate, order_probability as orderProbability, " +
            "pricing_elements_json as pricingElementsJson, created_time as createdTime, updated_time as updatedTime " +
            "FROM erp_item WHERE document_id = #{documentId} AND document_type = #{documentType} " +
            "ORDER BY item_no")
    List<Item> findItemsByDocumentIdAndType(@Param("documentId") Long documentId, 
                                           @Param("documentType") String documentType);

    /**
     * 插入项目
     * @param item 项目信息
     * @return 影响的行数
     */
    @Insert("INSERT INTO erp_item (document_id, document_type, item_no, mat_id, quantity, net_price, " +
            "item_value, plant_id, su, item_code, material_code, order_quantity_str, order_quantity_unit, " +
            "description, req_deliv_date, net_value_str, net_value_unit, tax_value_str, tax_value_unit, " +
            "pricing_date, order_probability, pricing_elements_json) " +
            "VALUES (#{documentId}, #{documentType}, #{itemNo}, #{matId}, #{quantity}, #{netPrice}, " +
            "#{itemValue}, #{plantId}, #{su}, #{itemCode}, #{materialCode}, #{orderQuantityStr}, " +
            "#{orderQuantityUnit}, #{description}, #{reqDelivDate}, #{netValueStr}, #{netValueUnit}, " +
            "#{taxValueStr}, #{taxValueUnit}, #{pricingDate}, #{orderProbability}, #{pricingElementsJson})")
    int insertItem(Item item);

    /**
     * 更新项目
     * @param item 项目信息
     * @return 影响的行数
     */
    @Update("UPDATE erp_item SET mat_id = #{matId}, quantity = #{quantity}, net_price = #{netPrice}, " +
            "item_value = #{itemValue}, plant_id = #{plantId}, su = #{su}, item_code = #{itemCode}, " +
            "material_code = #{materialCode}, order_quantity_str = #{orderQuantityStr}, " +
            "order_quantity_unit = #{orderQuantityUnit}, description = #{description}, " +
            "req_deliv_date = #{reqDelivDate}, net_value_str = #{netValueStr}, net_value_unit = #{netValueUnit}, " +
            "tax_value_str = #{taxValueStr}, tax_value_unit = #{taxValueUnit}, pricing_date = #{pricingDate}, " +
            "order_probability = #{orderProbability}, pricing_elements_json = #{pricingElementsJson} " +
            "WHERE document_id = #{documentId} AND document_type = #{documentType} AND item_no = #{itemNo}")
    int updateItem(Item item);

    /**
     * 删除文档的所有项目
     * @param documentId 文档ID
     * @param documentType 文档类型
     * @return 影响的行数
     */
    @Delete("DELETE FROM erp_item WHERE document_id = #{documentId} AND document_type = #{documentType}")
    int deleteItemsByDocumentIdAndType(@Param("documentId") Long documentId, 
                                      @Param("documentType") String documentType);

    /**
     * 删除单个项目
     * @param documentId 文档ID
     * @param documentType 文档类型
     * @param itemNo 项目号
     * @return 影响的行数
     */
    @Delete("DELETE FROM erp_item WHERE document_id = #{documentId} AND document_type = #{documentType} AND item_no = #{itemNo}")
    int deleteItemByDocumentIdTypeAndItemNo(@Param("documentId") Long documentId, 
                                           @Param("documentType") String documentType, 
                                           @Param("itemNo") Integer itemNo);

    /**
     * 检查项目是否存在
     * @param documentId 文档ID
     * @param documentType 文档类型
     * @param itemNo 项目号
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM erp_item WHERE document_id = #{documentId} AND document_type = #{documentType} AND item_no = #{itemNo}")
    int countByDocumentIdTypeAndItemNo(@Param("documentId") Long documentId, 
                                      @Param("documentType") String documentType, 
                                      @Param("itemNo") Integer itemNo);

    /**
     * 获取文档的项目数量
     * @param documentId 文档ID
     * @param documentType 文档类型
     * @return 项目数量
     */
    @Select("SELECT COUNT(*) FROM erp_item WHERE document_id = #{documentId} AND document_type = #{documentType}")
    int countItemsByDocumentIdAndType(@Param("documentId") Long documentId, 
                                     @Param("documentType") String documentType);

    /**
     * 获取物料描述 - 用于数据填充
     * @param materialId 物料ID
     * @return 物料描述
     */
    @Select("SELECT description FROM erp_material WHERE material_id = #{materialId}")
    String getMaterialDescription(@Param("materialId") Long materialId);
}
