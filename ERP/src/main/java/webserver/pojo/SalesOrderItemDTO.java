package webserver.pojo;




import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 销售订单项目数据传输对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SalesOrderItemDTO extends SalesOrderDetailDTO.Item {
    private String item;              // 项目号
    private String material;          // 物料号
    private String orderQuantity;     // 订单数量
    private String orderQuantityUnit; // 订单数量单位
    private String description;       // 描述
    private String reqDelivDate;      // 请求交货日期
    private String netValue;          // 净值
    private String netValueUnit;      // 净值单位
    private String taxValue;          // 税值
    private String taxValueUnit;      // 税值单位
    private String pricingDate;       // 定价日期
    private String orderProbability;  // 订单概率
}
