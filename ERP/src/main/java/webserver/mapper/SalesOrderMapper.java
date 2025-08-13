package webserver.mapper;

import org.apache.ibatis.annotations.*;
import webserver.pojo.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface SalesOrderMapper {
    List<Map<String, Object>> searchSalesOrders(SalesOrderSearchRequest request);
    
    // ✅ 新增方法：获取销售订单详情
    Map<String, Object> getSalesOrderDetails(@Param("soId") String soId);
    
    // ✅ 新增方法：获取销售订单项目列表
    List<Map<String, Object>> getSalesOrderItems(@Param("soId") String soId);

    // ✅ 新增方法：获取销售订单项目的定价元素
    List<Map<String, Object>> getPricingElements(@Param("soId") Long soId, @Param("itemNo") Integer itemNo);

    // ✅ 新增方法：获取询价单项目的定价元素
    List<Map<String, Object>> getInquiryPricingElements(@Param("inquiryId") Long inquiryId, @Param("itemNo") Integer itemNo);

    // ✅ 新增方法：获取报价单项目的定价元素
    List<Map<String, Object>> getQuotationPricingElements(@Param("quotationId") Long quotationId, @Param("itemNo") Integer itemNo);

    @Insert("INSERT INTO erp_sales_order_hdr (" +
            "quote_id, sold_tp, ship_tp, doc_date, req_delivery_date, " +
            "currency, net_value, tax_value, gross_value, incoterms, payment_terms, status" +
            ") VALUES (" +
            "#{quotationId}, #{soldTp}, #{shipTp}, NOW(), #{reqDeliveryDate}, " +
            "#{currency}, #{netValue}, #{taxValue}, #{grossValue}, #{incoterms}, #{paymentTerms}, 'NEW')")
    @Options(useGeneratedKeys = true, keyProperty = "soId")
    int insertSalesOrder(SalesOrder order);

    @Insert("INSERT INTO erp_sales_item (" +
            "so_id, item_no, mat_id, plt_id, storage_loc, quantity, su, net_price, discount_pct, status" +
            ") VALUES (" +
            "#{soId}, #{itemNo}, #{matId}, #{plantId}, #{storageLocation}, #{quantity}, #{unit}, #{netPrice}, 0, 'OPEN')")
    int insertSalesOrderItem(@Param("soId") Long soId,
                             @Param("itemNo") int itemNo,
                             @Param("matId") Long matId,
                             @Param("plantId") Long plantId,
                             @Param("storageLocation") String storageLocation,
                             @Param("quantity") int quantity,
                             @Param("unit") String unit,
                             @Param("netPrice") double netPrice);

    @Insert("INSERT INTO erp_pricing_element (" +
            "document_type, document_id, item_no, cnty, condition_name, amount, city, per_value, " +
            "uom, condition_value, currency, status, numC, ato_mts_component, oun, ccon_de, un, " +
            "condition_value2, cd_cur, stat" +
            ") VALUES (" +
            "'sales_order', #{documentId}, #{itemNo}, #{cnty}, #{conditionName}, #{amount}, #{city}, #{perValue}, " +
            "#{uom}, #{conditionValue}, #{currency}, #{status}, #{numC}, #{atoMtsComponent}, #{oun}, #{cconDe}, #{un}, " +
            "#{conditionValue2}, #{cdCur}, #{stat}" +
            ")")
    int insertPricingElement(PricingElement pricingElement);

    @Update("UPDATE erp_sales_order_hdr SET " +
            "quote_id = #{quotationId}, sold_tp = #{soldTp}, ship_tp = #{shipTp}, " +
            "req_delivery_date = #{reqDeliveryDate}, currency = #{currency}, " +
            "net_value = #{netValue}, tax_value = #{taxValue}, gross_value = #{grossValue}, " +
            "incoterms = #{incoterms}, payment_terms = #{paymentTerms} " +
            "WHERE so_id = #{soId}")
    int updateSalesOrder(SalesOrder order);
    
    @Update("UPDATE erp_sales_item SET " +
            "mat_id = #{matId}, plt_id = #{plantId}, storage_loc = #{storageLocation}, " +
            "quantity = #{quantity}, su = #{unit}, net_price = #{netPrice} " +
            "WHERE so_id = #{soId} AND item_no = #{itemNo}")
    int updateSalesOrderItem(@Param("soId") Long soId,
                             @Param("itemNo") int itemNo,
                             @Param("matId") Long matId,
                             @Param("plantId") Long plantId,
                             @Param("storageLocation") String storageLocation,
                             @Param("quantity") int quantity,
                             @Param("unit") String unit,
                             @Param("netPrice") double netPrice);
                             
    @Delete("DELETE FROM erp_sales_item WHERE so_id = #{soId}")
    int deleteSalesOrderItemsBySoId(@Param("soId") Long soId);

    @Delete("DELETE FROM erp_pricing_element WHERE document_type = #{documentType} AND document_id = #{documentId}")
    int deletePricingElements(@Param("documentType") String documentType, @Param("documentId") Long documentId);

    @Select("<script>" +
            "SELECT so.so_id AS soId, c.name AS customerName, c.name AS contactName, " +
            "so.req_delivery_date AS reqDeliveryDate, so.gross_value AS grossValue, " +
            "DATE_FORMAT(so.doc_date, '%Y-%m-%d') AS plannedCreationDate, " +
            "DATE_FORMAT(so.req_delivery_date, '%Y-%m-%d') AS plannedGIDate, " +
            "so.incoterms AS shippingPoint, so.ship_tp AS shipToParty, " +
            "so.gross_value AS grossWeight " +
            "FROM erp_sales_order_hdr so " +
            "LEFT JOIN erp_customer c ON so.sold_tp = c.customer_id " +
            "WHERE 1=1 " +
            "<if test='shipToParty != null and shipToParty != \"\"'>AND so.ship_tp = #{shipToParty} </if>" +
            "<if test='plannedCreationDate != null and plannedCreationDate != \"\"'>AND so.doc_date = #{plannedCreationDate} </if>" +
            "<if test='relevantForTM != null and relevantForTM != \"\"'>AND so.payment_terms = #{relevantForTM} </if>" +
            "</script>")
    List<SalesOrderSummaryDTO> selectSalesOrders(
            @Param("shipToParty") String shipToParty,
            @Param("plannedCreationDate") String plannedCreationDate,
            @Param("relevantForTM") String relevantForTM
    );

    @Select("<script>" +
            "SELECT " +
            "CAST(so.so_id AS CHAR) AS id, " +
            "DATE_FORMAT(so.req_delivery_date, '%Y-%m-%d') AS plannedCreationDate, " +
            "DATE_FORMAT(DATE_ADD(so.req_delivery_date, INTERVAL 7 DAY), '%Y-%m-%d') AS plannedGIDate, " +  // 注意逗号
            "CAST(so.customer_no AS CHAR) AS shipToParty, " +
            "CAST(so.customer_no AS CHAR) AS shippingPoint, " +
            "CAST(so.gross_value AS CHAR) AS grossWeight " +
            "FROM erp_sales_order_hdr so " +
            "LEFT JOIN erp_outbound_delivery od ON so.so_id = od.so_id " +
            "<where> " +
            "   <if test='shipToParty != null and shipToParty != \"\"'> " +
            "       AND so.customer_no = #{shipToParty} " +
            "   </if> " +
            "   <if test='plannedCreationDate != null and plannedCreationDate != \"\"'> " +
            "       AND so.req_delivery_date = #{plannedCreationDate} " +
            "   </if> " +
            "   AND od.so_id IS NULL " +
            "</where>" +
            "</script>")
    List<OrderSummary> selectSalesOrders1(
            @Param("shipToParty") String shipToParty,
            @Param("plannedCreationDate") String plannedCreationDate,
            @Param("relevantForTM") String relevantForTM
    );
}

