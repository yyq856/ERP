package webserver.pojo;

import lombok.Data;
import java.util.List;

@Data
public class QuotationItemDTO {
    // 基础字段
    private String item;                    // 项目号
    private String material;                // 物料号
    private String orderQuantity;           // 订单数量
    private String orderQuantityUnit;       // 订单数量单位
    private String description;             // 物料描述
    private String reqDelivDate;            // 要求交货日期 (YYYY-MM-DD)
    private String netValue;                // 净值
    private String netValueUnit;            // 净值单位
    private String taxValue;                // 税值
    private String taxValueUnit;            // 税值单位
    private String pricingDate;             // 定价日期 (YYYY-MM-DD)
    private String orderProbability;        // 订单概率
    private List<PricingElementDTO> pricingElements; // 定价元素列表

    // 兼容性字段
    private String su;                      // 销售单位
    private int altItm;                     // 替代项目
    private Float netPrice;                 // 净价格
}
