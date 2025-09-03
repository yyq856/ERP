package webserver.service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 销售订单金额计算服务接口
 */
public interface SalesOrderCalculationService {

    /**
     * 重新计算并更新销售订单头表的金额
     * @param soId 销售订单ID
     * @return 是否更新成功
     */
    boolean recalculateAndUpdateSalesOrderAmounts(Long soId);

    /**
     * 批量更新所有销售订单的金额
     * @return 更新的订单数量
     */
    int batchUpdateAllSalesOrderAmounts();

    /**
     * 计算销售订单的金额（不更新数据库）
     * @param soId 销售订单ID
     * @return 计算结果 {netValue, taxValue, grossValue, currency}
     */
    SalesOrderAmountResult calculateSalesOrderAmounts(Long soId);

    /**
     * 获取销售订单信息
     * @param soId 销售订单ID
     * @return 销售订单信息
     */
    Map<String, Object> getSalesOrderInfo(Long soId);

    /**
     * 销售订单金额计算结果
     */
    class SalesOrderAmountResult {
        private BigDecimal netValue;
        private BigDecimal taxValue;
        private BigDecimal grossValue;
        private String currency;

        public SalesOrderAmountResult() {}

        public SalesOrderAmountResult(BigDecimal netValue, BigDecimal taxValue, BigDecimal grossValue, String currency) {
            this.netValue = netValue;
            this.taxValue = taxValue;
            this.grossValue = grossValue;
            this.currency = currency;
        }

        // Getters and Setters
        public BigDecimal getNetValue() { return netValue; }
        public void setNetValue(BigDecimal netValue) { this.netValue = netValue; }

        public BigDecimal getTaxValue() { return taxValue; }
        public void setTaxValue(BigDecimal taxValue) { this.taxValue = taxValue; }

        public BigDecimal getGrossValue() { return grossValue; }
        public void setGrossValue(BigDecimal grossValue) { this.grossValue = grossValue; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        @Override
        public String toString() {
            return String.format("SalesOrderAmountResult{netValue=%s, taxValue=%s, grossValue=%s, currency='%s'}", 
                netValue, taxValue, grossValue, currency);
        }
    }
}
