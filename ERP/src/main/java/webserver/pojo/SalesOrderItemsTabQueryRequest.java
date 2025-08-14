package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 销售订单物品条件查询请求类
 * 扩展支持完整的 ItemValidation 字段结构
 */
public class SalesOrderItemsTabQueryRequest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemQuery {
        private String item;                    // 项目号
        private String material;                // 物料号
        private String orderQuantity;           // 订单数量
        private String orderQuantityUnit;       // 订单数量单位
        private String description;             // 物料描述
        private String reqDelivDate;            // 要求交货日期
        private String netValue;                // 净值
        private String netValueUnit;            // 净值单位
        private String taxValue;                // 税值
        private String taxValueUnit;            // 税值单位
        private String pricingDate;             // 定价日期
        private String orderProbability;        // 订单概率
    }
}
