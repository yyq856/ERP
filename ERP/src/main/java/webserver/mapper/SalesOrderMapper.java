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





    @Update("UPDATE erp_sales_order_hdr SET " +
            "quote_id = #{quotationId}, sold_tp = #{soldTp}, ship_tp = #{shipTp}, " +
            "req_delivery_date = #{reqDeliveryDate}, currency = #{currency}, " +
            "net_value = #{netValue}, tax_value = #{taxValue}, gross_value = #{grossValue}, " +
            "incoterms = #{incoterms}, payment_terms = #{paymentTerms} " +
            "WHERE so_id = #{soId}")
    int updateSalesOrder(SalesOrder order);
    

                             


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
            "DATE_FORMAT(DATE_ADD(so.req_delivery_date, INTERVAL 7 DAY), '%Y-%m-%d') AS plannedGIDate, " +
            "COALESCE(so.incoterms, '1000') AS shippingPoint, " +  // 使用 incoterms 作为发货处，默认 1000
            "COALESCE(c2.name, CAST(so.ship_tp AS CHAR)) AS shipToParty, " +  // 使用客户名称，回退到ID
            "CAST(so.gross_value AS CHAR) AS grossWeight, " +
            "FORMAT(so.net_value, 2) AS netValue, " +  // 新增：净值
            "so.currency AS currency " +  // 新增：货币
            "FROM erp_sales_order_hdr so " +
            "LEFT JOIN erp_customer c2 ON so.ship_tp = c2.customer_id " +  // 关联送达方客户
            "LEFT JOIN erp_outbound_delivery od ON so.so_id = od.so_id " +
            "<where> " +
            "   <if test='shipToParty != null and shipToParty != \"\"'> " +
            "       AND so.ship_tp = #{shipToParty} " +  // 修正：使用 ship_tp 而不是 customer_no
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

