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


    // 报价单明细表查询
    @Select("""
                SELECT 
                    quotation_id AS quotationId,
                    item_no AS itemNo,
                    mat_id AS matId,
                    quantity,
                    net_price AS netPrice,
                    item_discount_pct AS itemDiscountPct,
                    item_value AS itemValue,
                    plant_id AS plantId,
                    su,
                    cnty,
                    tax_value AS taxValue
                FROM erp_quotation_item
                WHERE quotation_id = #{quotationId}
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

}