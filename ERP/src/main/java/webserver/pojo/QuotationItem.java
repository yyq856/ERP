package webserver.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class QuotationItem {
    private Integer itemNo; // 项目号
    private String material; // 物料号
    private Integer quantity; // 数量
    private String unit; // 单位
    private String description; // 描述
    private LocalDate reqDelivDate; // 请求交货日期
    private BigDecimal netValue; // 净值
    private String netValueUnit; // 净值单位
    private BigDecimal taxValue; // 税值
    private String taxValueUnit; // 税值单位
    private LocalDate pricingDate; // 定价日期
    private String orderProbability; // 订单概率
}
