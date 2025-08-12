package webserver.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class DashboardDTO {
    // 1. 核心业绩总览
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Overview {
        private int newOrders;
        private double turnover;
        private int shipments;
        private String period; // today | week
        private LocalDate startDate;
        private LocalDate endDate;
    }

    // 2. Top 3 贡献客户
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerContribution {
        private String customerNo;
        private double totalValue;
    }

    // 3. 畅销/滞销物料
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialStat {
        private String matId;
        private double totalQty;
    }

    // 4. 紧急交付预警
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UrgentOrder {
        private String soId;
        private String customerNo;
        private LocalDate reqDeliveryDate;
        private double netValue;
    }

    // 5. 财务风险
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialRisk {
        private double overdueAmount;
        private LocalDate monthStart;
        private LocalDate today;
    }

    // 6. 营收对比
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueComparison {
        private double lastWeekRevenue;
        private double thisWeekRevenue;
        private LocalDate lastWeekStart;
        private LocalDate lastWeekEnd;
        private LocalDate thisWeekStart;
        private LocalDate today;
    }

    // 汇总载体（可用于一次性返回所有卡片数据）
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllCards {
        private Overview overview;
        private List<CustomerContribution> topCustomers;
        private List<MaterialStat> topMaterials;
        private List<MaterialStat> bottomMaterials;
        private List<UrgentOrder> urgentOrders;
        private FinancialRisk financialRisk;
        private RevenueComparison revenueComparison;
    }
}

