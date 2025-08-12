package webserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import webserver.dashboard.DashboardDTO;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DashboardMapper {

    @Select("SELECT COUNT(so_id) FROM erp_sales_order_hdr WHERE doc_date >= #{start}")
    Integer countNewOrders(@Param("start") LocalDate periodStart);

    @Select("SELECT COALESCE(SUM(net_value), 0) FROM erp_sales_order_hdr WHERE doc_date >= #{start}")
    Double sumOrderNetValue(@Param("start") LocalDate periodStart);

    @Select("SELECT COUNT(gi_id) FROM erp_good_issue WHERE posting_date >= #{start}")
    Integer countShipments(@Param("start") LocalDate periodStart);

    // 最近5个工作日内Top 3客户（按订单净值）
    @Select("\n" +
            "SELECT CAST(h.customer_no AS CHAR) AS customerNo, COALESCE(SUM(h.net_value),0) AS totalValue\n" +
            "FROM erp_sales_order_hdr h\n" +
            "WHERE h.doc_date >= #{start} AND DAYOFWEEK(h.doc_date) NOT IN (1,7)\n" +
            "GROUP BY h.customer_no\n" +
            "ORDER BY totalValue DESC\n" +
            "LIMIT 3")
    List<DashboardDTO.CustomerContribution> topCustomers(@Param("start") LocalDate startDate);

    // 畅销物料 Top N
    @Select("\n" +
            "SELECT CAST(i.mat_id AS CHAR) AS matId, COALESCE(SUM(i.quantity),0) AS totalQty\n" +
            "FROM erp_sales_item i\n" +
            "JOIN erp_sales_order_hdr h ON h.so_id = i.so_id\n" +
            "WHERE h.doc_date >= #{start}\n" +
            "GROUP BY i.mat_id\n" +
            "ORDER BY totalQty DESC\n" +
            "LIMIT #{limit}")
    List<DashboardDTO.MaterialStat> topMaterials(@Param("start") LocalDate periodStart, @Param("limit") int limit);

    // 滞销物料 Bottom N
    @Select("\n" +
            "SELECT CAST(i.mat_id AS CHAR) AS matId, COALESCE(SUM(i.quantity),0) AS totalQty\n" +
            "FROM erp_sales_item i\n" +
            "JOIN erp_sales_order_hdr h ON h.so_id = i.so_id\n" +
            "WHERE h.doc_date >= #{start}\n" +
            "GROUP BY i.mat_id\n" +
            "ORDER BY totalQty ASC\n" +
            "LIMIT #{limit}")
    List<DashboardDTO.MaterialStat> bottomMaterials(@Param("start") LocalDate periodStart, @Param("limit") int limit);

    // 紧急交付预警：按要求交货日期排序，筛选未发货（无GI）
    @Select("\n" +
            "SELECT CAST(h.so_id AS CHAR) AS soId, CAST(h.customer_no AS CHAR) AS customerNo, h.req_delivery_date AS reqDeliveryDate, COALESCE(h.net_value,0) AS netValue\n" +
            "FROM erp_sales_order_hdr h\n" +
            "WHERE NOT EXISTS (\n" +
            "  SELECT 1 FROM erp_outbound_delivery od\n" +
            "  JOIN erp_good_issue gi ON gi.dlv_id = od.dlv_id\n" +
            "  WHERE od.so_id = h.so_id\n" +
            ")\n" +
            "ORDER BY h.req_delivery_date ASC\n" +
            "LIMIT #{limit}")
    List<DashboardDTO.UrgentOrder> urgentOrders(@Param("limit") int limit);

    // 财务风险：当月开票，逾期未清账金额汇总（按so.payment_terms计算到期日）
    @Select("\n" +
            "SELECT COALESCE(SUM(GREATEST(b.gross - IFNULL(p.paid, 0), 0)), 0)\n" +
            "FROM erp_billing_hdr b\n" +
            "JOIN erp_outbound_delivery od ON od.dlv_id = b.dlv_id\n" +
            "JOIN erp_sales_order_hdr so ON so.so_id = od.so_id\n" +
            "LEFT JOIN (\n" +
            "  SELECT bill_id, COALESCE(SUM(amount),0) AS paid\n" +
            "  FROM erp_payment\n" +
            "  GROUP BY bill_id\n" +
            ") p ON p.bill_id = b.bill_id\n" +
            "WHERE b.billing_date BETWEEN #{monthStart} AND #{today}\n" +
            "  AND DATE_ADD(b.billing_date, INTERVAL CASE so.payment_terms WHEN '0001' THEN 30 WHEN '0002' THEN 60 WHEN '0003' THEN 0 ELSE 0 END DAY) < #{today}\n" +
            "  AND IFNULL(p.paid,0) < b.gross")
    Double calcFinancialRisk(@Param("monthStart") LocalDate monthStart, @Param("today") LocalDate today);

    // 付款总额（用于营收对比）
    @Select("SELECT COALESCE(SUM(amount),0) FROM erp_payment WHERE posting_date BETWEEN #{start} AND #{end}")
    Double sumPaymentsBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}

