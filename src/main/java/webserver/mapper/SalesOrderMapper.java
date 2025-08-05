package webserver.mapper;

import org.apache.ibatis.annotations.*;
import webserver.pojo.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface SalesOrderMapper {
    List<Map<String, Object>> searchSalesOrders(@Param("request") SalesOrderSearchRequest request);
    
    // ✅ 新增方法：获取销售订单详情
    Map<String, Object> getSalesOrderDetails(@Param("soId") String soId);
    
    // ✅ 新增方法：获取销售订单项目列表
    List<Map<String, Object>> getSalesOrderItems(@Param("soId") String soId);

    @Insert("INSERT INTO erp_sales_order_hdr (" +
            "quote_id, customer_no, contact_id, doc_date, req_delivery_date, " +
            "currency, net_value, tax_value, gross_value, incoterms, payment_terms, status" +
            ") VALUES (" +
            "#{quotationId}, #{customerId}, #{contactId}, NOW(), #{reqDeliveryDate}, " +
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
                             
    @Update("UPDATE erp_sales_order_hdr SET " +
            "quote_id = #{quotationId}, customer_no = #{customerId}, contact_id = #{contactId}, " +
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

    @Select("<script>" +
            "SELECT so.id, so.planned_creation_date AS plannedCreationDate, so.planned_gi_date AS plannedGIDate, " +
            "so.shipping_point AS shippingPoint, so.ship_to_party AS shipToParty, " +
            "so.gross_weight AS grossWeight " +
            "FROM erp_sales_order so " +
            "WHERE 1=1 " +
            "<if test='shipToParty != null and shipToParty != \"\"'>AND so.ship_to_party = #{shipToParty} </if>" +
            "<if test='plannedCreationDate != null and plannedCreationDate != \"\"'>AND so.planned_creation_date = #{plannedCreationDate} </if>" +
            "<if test='relevantForTM != null and relevantForTM != \"\"'>AND so.relevant_for_tm = #{relevantForTM} </if>" +
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
            "DATE_FORMAT(od.gi_date, '%Y-%m-%d') AS plannedGIDate, " +
            "od.shipping_point AS shippingPoint, " +
            "CAST(so.customer_no AS CHAR) AS shipToParty, " +
            "'' AS grossWeight " +
            "FROM erp_sales_order_hdr so " +
            "LEFT JOIN erp_outbound_delivery od ON so.so_id = od.so_id " +
            "WHERE 1=1 " +
            "<if test='shipToParty != null and shipToParty != \"\"'> AND so.customer_no = #{shipToParty} </if> " +
            "<if test='plannedCreationDate != null and plannedCreationDate != \"\"'> AND so.req_delivery_date = #{plannedCreationDate} </if> " +
            "</script>")
    List<SalesOrderDetailDTO> selectSalesOrders1(
            @Param("shipToParty") String shipToParty,
            @Param("plannedCreationDate") String plannedCreationDate,
            @Param("relevantForTM") String relevantForTM
    );
}

