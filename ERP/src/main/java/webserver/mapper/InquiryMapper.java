package webserver.mapper;

import org.apache.ibatis.annotations.*;
import webserver.pojo.Inquiry;
import webserver.pojo.InquiryItem;

import java.util.List;

@Mapper
public interface InquiryMapper {


        // ...existing code...
        /**
         * 删除单个询价单项目
         * @param inquiryId 询价单ID
         * @param itemNo 项目号
         * @return 影响的行数
         */
        @Delete("DELETE FROM erp_item WHERE document_id = #{inquiryId} AND document_type = 'inquiry' AND item_no = #{itemNo}")
        int deleteInquiryItemByItemNo(@Param("inquiryId") Long inquiryId, @Param("itemNo") Integer itemNo);

        /**
         * 更新单个询价单项目
         * @param item 询价单项目
         * @return 影响的行数
         */
        @Update("UPDATE erp_item SET mat_id = #{matId}, quantity = #{quantity}, net_price = #{netPrice}, item_value = #{itemValue}, plant_id = #{plantId}, su = #{su}, item_code = #{itemCode}, material_code = #{materialCode}, order_quantity_str = #{orderQuantityStr}, order_quantity_unit = #{orderQuantityUnit}, description = #{description}, req_deliv_date = #{reqDelivDate}, net_value_str = #{netValueStr}, net_value_unit = #{netValueUnit}, tax_value_str = #{taxValueStr}, tax_value_unit = #{taxValueUnit}, pricing_date = #{pricingDate}, order_probability = #{orderProbability}, pricing_elements_json = #{pricingElementsJson} WHERE document_id = #{inquiryId} AND document_type = 'inquiry' AND item_no = #{itemNo}")
        int updateInquiryItem(InquiryItem item);
    
    /**
     * 根据询价单ID查询询价单
     * @param inquiryId 询价单ID
     * @return 询价单信息
     */
    @Select("SELECT inquiry_id as inquiryId, cust_id as custId, inquiry_type as inquiryType, " +
            "sls_org as slsOrg, sales_district as salesDistrict, division, sold_tp as soldTp, " +
            "ship_tp as shipTp, cust_ref as custRef, customer_reference_date as customerReferenceDate, " +
            "valid_from_date as validFromDate, valid_to_date as validToDate, req_deliv_date as reqDelivDate, probability, " +
            "net_value as netValue, status " +
            "FROM erp_inquiry WHERE inquiry_id = #{inquiryId}")
    Inquiry findByInquiryId(@Param("inquiryId") Long inquiryId);
    
    /**
     * 插入新的询价单
     * @param inquiry 询价单信息
     * @return 影响的行数
     */
    @Insert("INSERT INTO erp_inquiry (cust_id, inquiry_type, sls_org, sales_district, division, " +
            "sold_tp, ship_tp, cust_ref, customer_reference_date, valid_from_date, valid_to_date, probability, net_value, status, req_deliv_date) " +
            "VALUES (#{custId}, #{inquiryType}, #{slsOrg}, #{salesDistrict}, #{division}, " +
            "#{soldTp}, #{shipTp}, #{custRef}, #{customerReferenceDate}, #{validFromDate}, " +
            "#{validToDate}, #{probability}, #{netValue}, #{status}, #{reqDelivDate})")
    @Options(useGeneratedKeys = true, keyProperty = "inquiryId")
    int insertInquiry(Inquiry inquiry);
    
    /**
     * 更新询价单
     * @param inquiry 询价单信息
     * @return 影响的行数
     */
    @Update("UPDATE erp_inquiry SET cust_id = #{custId}, inquiry_type = #{inquiryType}, " +
            "sls_org = #{slsOrg}, sales_district = #{salesDistrict}, division = #{division}, " +
            "sold_tp = #{soldTp}, ship_tp = #{shipTp}, cust_ref = #{custRef}, " +
            "customer_reference_date = #{customerReferenceDate}, valid_from_date = #{validFromDate}, " +
            "valid_to_date = #{validToDate}, probability = #{probability}, net_value = #{netValue}, status = #{status}, req_deliv_date = #{reqDelivDate} WHERE inquiry_id = #{inquiryId}")
    int updateInquiry(Inquiry inquiry);
    
    /**
     * 根据询价单ID查询询价单项目 - 扩展支持完整字段
     * @param inquiryId 询价单ID
     * @return 询价单项目列表
     */
    @Select("SELECT document_id as inquiryId, item_no as itemNo, mat_id as matId, quantity, " +
            "net_price as netPrice, item_value as itemValue, plant_id as plantId, su, " +
            "item_code as itemCode, material_code as materialCode, order_quantity_str as orderQuantityStr, " +
            "order_quantity_unit as orderQuantityUnit, description, req_deliv_date as reqDelivDate, " +
            "net_value_str as netValueStr, net_value_unit as netValueUnit, tax_value_str as taxValueStr, " +
            "tax_value_unit as taxValueUnit, pricing_date as pricingDate, order_probability as orderProbability, " +
            "pricing_elements_json as pricingElementsJson " +
            "FROM erp_item WHERE document_id = #{inquiryId} AND document_type = 'inquiry' ORDER BY item_no")
    List<InquiryItem> findItemsByInquiryId(@Param("inquiryId") Long inquiryId);
    
    /**
     * 插入询价单项目 - 扩展支持完整字段
     * @param item 询价单项目
     * @return 影响的行数
     */
    @Insert("INSERT INTO erp_item (document_id, document_type, item_no, mat_id, quantity, net_price, " +
            "item_value, plant_id, su, item_code, material_code, order_quantity_str, " +
            "order_quantity_unit, description, req_deliv_date, net_value_str, net_value_unit, " +
            "tax_value_str, tax_value_unit, pricing_date, order_probability, pricing_elements_json) " +
            "VALUES (#{inquiryId}, 'inquiry', #{itemNo}, #{matId}, #{quantity}, #{netPrice}, #{itemValue}, " +
            "#{plantId}, #{su}, #{itemCode}, #{materialCode}, #{orderQuantityStr}, " +
            "#{orderQuantityUnit}, #{description}, #{reqDelivDate}, #{netValueStr}, #{netValueUnit}, " +
            "#{taxValueStr}, #{taxValueUnit}, #{pricingDate}, #{orderProbability}, #{pricingElementsJson})")
    int insertInquiryItem(InquiryItem item);
    
    /**
     * 删除询价单的所有项目
     * @param inquiryId 询价单ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM erp_item WHERE document_id = #{inquiryId} AND document_type = 'inquiry'")
    int deleteInquiryItems(@Param("inquiryId") Long inquiryId);

    /**
     * 删除文档的所有项目 - 使用统一的erp_item表（通用方法）
     * @param documentId 文档ID
     * @param documentType 文档类型
     * @return 影响的行数
     */
    @Delete("DELETE FROM erp_item WHERE document_id = #{documentId} AND document_type = #{documentType}")
    int deleteDocumentItemsFromUnifiedTable(@Param("documentId") Long documentId, @Param("documentType") String documentType);

    /**
     * 插入文档项目到统一的erp_item表（通用方法）
     * @param item 项目实体
     * @param documentType 文档类型
     * @return 影响的行数
     */
    @Insert("INSERT INTO erp_item (document_id, document_type, item_no, mat_id, quantity, net_price, item_value, " +
            "plant_id, su, item_code, material_code, order_quantity_str, order_quantity_unit, description, req_deliv_date, " +
            "net_value_str, net_value_unit, tax_value_str, tax_value_unit, pricing_date, order_probability, pricing_elements_json) " +
            "VALUES (#{item.inquiryId}, #{documentType}, #{item.itemNo}, #{item.matId}, #{item.quantity}, #{item.netPrice}, #{item.itemValue}, " +
            "#{item.plantId}, #{item.su}, #{item.itemCode}, #{item.materialCode}, #{item.orderQuantityStr}, " +
            "#{item.orderQuantityUnit}, #{item.description}, #{item.reqDelivDate}, #{item.netValueStr}, #{item.netValueUnit}, " +
            "#{item.taxValueStr}, #{item.taxValueUnit}, #{item.pricingDate}, #{item.orderProbability}, #{item.pricingElementsJson})")
    int insertDocumentItemToUnifiedTable(@Param("item") InquiryItem item, @Param("documentType") String documentType);
    
    /**
     * 检查询价单是否存在
     * @param inquiryId 询价单ID
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM erp_inquiry WHERE inquiry_id = #{inquiryId}")
    int countByInquiryId(@Param("inquiryId") Long inquiryId);
    
    /**
     * 获取物料描述 - 用于数据填充
     * @param materialId 物料ID
     * @return 物料描述
     */
    @Select("SELECT description FROM erp_material WHERE material_id = #{materialId}")
    String getMaterialDescription(@Param("materialId") Long materialId);
    
    /**
     * 获取物料基本单位 - 用于数据填充
     * @param materialId 物料ID
     * @return 基本单位
     */
    @Select("SELECT base_unit FROM erp_material WHERE material_id = #{materialId}")
    String getMaterialBaseUnit(@Param("materialId") Long materialId);
}
