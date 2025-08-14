package webserver.mapper;

import lombok.Data;
import org.apache.ibatis.annotations.*;
import webserver.pojo.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface QuotationMapper {

    InquiryDTO selectInquiryById(@Param("inquiryId") String inquiryId);

    List<InquiryItemDTO> selectInquiryItemsById(@Param("inquiryId") String inquiryId);

    void insertQuotationFromInquiry(InquiryDTO inquiry);

    void insertQuotationItemsFromInquiry(@Param("quotationId") Long quotationId,
                                         @Param("items") List<InquiryItemDTO> items);

    Long getLastInsertId();

    // 报价单主表查询
    @Select("""
                SELECT 
                    quotation_id AS quotationId,
                    reference_inquiry_id AS referenceInquiryId,
                    cust_id AS custId,
                    inquiry_type AS inquiryType,
                    sls_org AS slsOrg,
                    sales_district AS salesDistrict,
                    division AS division,
                    sold_tp AS soldTp,
                    ship_tp AS shipTp,
                    cust_ref AS custRef,
                    customer_reference_date AS customerReferenceDate,
                    valid_from_date AS validFromDate,
                    valid_to_date AS validToDate,
                    probability,
                    net_value AS netValue,
                    status,
                    currency
                FROM erp_quotation
                WHERE quotation_id = #{quotationId}
            """)
    QuotationDTO selectQuotationById(@Param("quotationId") String quotationId);


    // 从erp_item表直接查询报价单相关物料信息
    @Select("""
                SELECT
                    document_id AS quotationId,
                    item_no AS itemNo,
                    mat_id AS matId,
                    quantity,
                    net_price AS netPrice,
                    0 AS itemDiscountPct,
                    item_value AS itemValue,
                    plant_id AS plantId,
                    su,
                    '' AS cnty,
                    CAST(tax_value_str AS DECIMAL(10,2)) AS taxValue,
                    description
                FROM erp_item
                WHERE document_id = #{quotationId} AND document_type = 'quotation'
            """)
    List<QuotationItemEntity> selectQuotationItemsByQuotationId(@Param("quotationId") String quotationId);

    @Update("""
        UPDATE erp_quotation
        SET sold_tp = #{basicInfo.soldToParty},
            ship_tp = #{basicInfo.shipToParty},
            cust_ref = #{basicInfo.customerReference},
            net_value = #{basicInfo.netValue},
            currency = #{basicInfo.netValueUnit},
            customer_reference_date = #{basicInfo.customerReferenceDate},
            valid_from_date = #{itemOverview.validFrom},
            valid_to_date = #{itemOverview.validTo}
        WHERE quotation_id = #{quotationId}
    """)
    void updateQuotation(@Param("quotationId") Long quotationId,
                         @Param("basicInfo") QuotationDetailsResponseDTO.BasicInfo basicInfo,
                         @Param("itemOverview") QuotationDetailsResponseDTO.ItemOverview itemOverview);

    @Delete("DELETE FROM erp_quotation_item WHERE quotation_id = #{quotationId}")
    void deleteQuotationItems(@Param("quotationId") Long quotationId);

    @Update("UPDATE erp_quotation_item " +
            "SET mat_id = #{item.material}, " +
            "quantity = #{item.orderQuantity}, " +
            "su = #{item.su}, " +
            "net_price = #{item.netPrice}, " +
            "item_value = #{item.orderQuantity} * #{item.netPrice} " +
            "WHERE quotation_id = #{quotationId} AND item_no = #{item.item}")
    void updateQuotationItem(@Param("quotationId") Long quotationId,
                             @Param("item") QuotationItemDTO item);

    @Select({
            "<script>",
            "SELECT quotation_id AS salesQuotation, sold_tp AS soldToParty, cust_ref AS customerReference,",
            "status AS overallStatus, valid_to_date AS latestExpiration",
            "FROM erp_quotation",
            "WHERE 1=1",
            "<if test='customerReference != null and customerReference != \"\"'>",
            "AND cust_ref = #{customerReference}",
            "</if>",
            "<if test='latestExpiration != null and latestExpiration != \"\"'>",
            "AND valid_to_date &lt;= #{latestExpiration}",
            "</if>",
            "<if test='overallStatus != null and overallStatus != \"\"'>",
            "AND status = #{overallStatus}",
            "</if>",
            "<if test='salesQuotation != null and salesQuotation != \"\"'>",
            "AND quotation_id = #{salesQuotation}",
            "</if>",
            "<if test='soldToParty != null and soldToParty != \"\"'>",
            "AND sold_tp = #{soldToParty}",
            "</if>",
            "</script>"
    })
    List<Map<String, Object>> searchQuotations(QuotationSearchRequestDTO request);

    // ========== 新增：使用统一erp_item表的方法 ==========

    /**
     * 根据报价单ID查询报价单项目 - 使用统一的erp_item表
     * @param quotationId 报价单ID
     * @return 报价单项目列表
     */
    @Select("SELECT document_id as quotationId, item_no as itemNo, mat_id as matId, quantity, " +
            "net_price as netPrice, item_value as itemValue, plant_id as plantId, su, " +
            "item_code as itemCode, material_code as materialCode, order_quantity_str as orderQuantityStr, " +
            "order_quantity_unit as orderQuantityUnit, description, req_deliv_date as reqDelivDate, " +
            "net_value_str as netValueStr, net_value_unit as netValueUnit, tax_value_str as taxValueStr, " +
            "tax_value_unit as taxValueUnit, pricing_date as pricingDate, order_probability as orderProbability, " +
            "pricing_elements_json as pricingElementsJson " +
            "FROM erp_item WHERE document_id = #{quotationId} AND document_type = 'quotation' ORDER BY item_no")
    List<QuotationItemEntity> findItemsByQuotationIdFromUnifiedTable(@Param("quotationId") Long quotationId);

    /**
     * 插入报价单项目 - 使用统一的erp_item表
     * @param item 报价单项目
     * @return 影响的行数
     */
    @Insert("INSERT INTO erp_item (document_id, document_type, item_no, mat_id, quantity, net_price, " +
            "item_value, plant_id, su, item_code, material_code, order_quantity_str, " +
            "order_quantity_unit, description, req_deliv_date, net_value_str, net_value_unit, " +
            "tax_value_str, tax_value_unit, pricing_date, order_probability, pricing_elements_json) " +
            "VALUES (#{quotationId}, 'quotation', #{itemNo}, #{matId}, #{quantity}, #{netPrice}, #{itemValue}, " +
            "#{plantId}, #{su}, #{itemCode}, #{materialCode}, #{orderQuantityStr}, " +
            "#{orderQuantityUnit}, #{description}, #{reqDelivDate}, #{netValueStr}, #{netValueUnit}, " +
            "#{taxValueStr}, #{taxValueUnit}, #{pricingDate}, #{orderProbability}, #{pricingElementsJson})")
    int insertQuotationItemToUnifiedTable(QuotationItemEntity item);

    /**
     * 插入文档项目到统一的erp_item表（通用方法）
     * @param item 项目实体
     * @param documentType 文档类型
     * @return 影响的行数
     */
    @Insert("INSERT INTO erp_item (document_id, document_type, item_no, mat_id, quantity, net_price, item_value, " +
            "plant_id, su, item_code, material_code, order_quantity_str, order_quantity_unit, description, req_deliv_date, " +
            "net_value_str, net_value_unit, tax_value_str, tax_value_unit, pricing_date, order_probability, pricing_elements_json) " +
            "VALUES (#{item.quotationId}, #{documentType}, #{item.itemNo}, #{item.matId}, #{item.quantity}, #{item.netPrice}, #{item.itemValue}, " +
            "#{item.plantId}, #{item.su}, #{item.itemCode}, #{item.materialCode}, #{item.orderQuantityStr}, " +
            "#{item.orderQuantityUnit}, #{item.description}, #{item.reqDelivDate}, #{item.netValueStr}, #{item.netValueUnit}, " +
            "#{item.taxValueStr}, #{item.taxValueUnit}, #{item.pricingDate}, #{item.orderProbability}, #{item.pricingElementsJson})")
    int insertDocumentItemToUnifiedTable(@Param("item") QuotationItemEntity item, @Param("documentType") String documentType);

    /**
     * 更新报价单项目 - 使用统一的erp_item表
     * @param item 报价单项目
     * @return 影响的行数
     */
    @Update("UPDATE erp_item SET mat_id = #{matId}, quantity = #{quantity}, net_price = #{netPrice}, " +
            "item_value = #{itemValue}, plant_id = #{plantId}, su = #{su}, item_code = #{itemCode}, " +
            "material_code = #{materialCode}, order_quantity_str = #{orderQuantityStr}, " +
            "order_quantity_unit = #{orderQuantityUnit}, description = #{description}, " +
            "req_deliv_date = #{reqDelivDate}, net_value_str = #{netValueStr}, net_value_unit = #{netValueUnit}, " +
            "tax_value_str = #{taxValueStr}, tax_value_unit = #{taxValueUnit}, pricing_date = #{pricingDate}, " +
            "order_probability = #{orderProbability}, pricing_elements_json = #{pricingElementsJson} " +
            "WHERE document_id = #{quotationId} AND document_type = 'quotation' AND item_no = #{itemNo}")
    int updateQuotationItemInUnifiedTable(QuotationItemEntity item);

    /**
     * 删除报价单的所有项目 - 使用统一的erp_item表
     * @param quotationId 报价单ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM erp_item WHERE document_id = #{quotationId} AND document_type = 'quotation'")
    int deleteQuotationItemsFromUnifiedTable(@Param("quotationId") Long quotationId);

    /**
     * 删除文档的所有项目 - 使用统一的erp_item表（通用方法）
     * @param documentId 文档ID
     * @param documentType 文档类型
     * @return 影响的行数
     */
    @Delete("DELETE FROM erp_item WHERE document_id = #{documentId} AND document_type = #{documentType}")
    int deleteDocumentItemsFromUnifiedTable(@Param("documentId") Long documentId, @Param("documentType") String documentType);

}