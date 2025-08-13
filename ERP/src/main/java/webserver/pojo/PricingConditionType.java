package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 定价条件类型实体类
 * 对应数据库表 erp_pricing_condition_type
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingConditionType {
    private String cnty;                    // 条件类型代码
    private String name;                    // 条件类型名称
    private String description;             // 条件描述
    private Boolean isPercentage;           // 是否为百分比类型
    private String defaultCurrency;         // 默认货币
    private Boolean isActive;               // 是否有效
}