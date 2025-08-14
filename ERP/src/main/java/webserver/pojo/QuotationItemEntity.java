package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报价单项目实体类 - 扩展版本
 * 支持完整的ItemValidation字段结构，用于与统一的erp_item表交互
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuotationItemEntity {
    // 主键字段
    private Long quotationId;         // 报价单ID
    private Integer itemNo;           // 项目号

    // 基础字段
    private Long matId;               // 物料ID (数字形式，用于数据库关联)
    private Integer quantity;         // 数量 (数字形式)
    private Float netPrice;           // 净价 (数字形式)
    private Float itemValue;          // 项目总值
    private Long plantId;             // 工厂ID
    private String su;                // 单位

    // 保留的旧字段（向后兼容）
    private Integer itemDiscountPct;  // 项目折扣百分比
    private Integer cnty;             // 国家代码
    private Float taxValue;           // 税值

    // ItemValidation 完整字段结构
    private String itemCode;          // 项目代码 (对应 item 字段)
    private String materialCode;      // 物料代码 (对应 material 字段)
    private String orderQuantityStr;  // 订单数量字符串 (对应 orderQuantity 字段)
    private String orderQuantityUnit; // 订单数量单位 (对应 orderQuantityUnit 字段)
    private String description;       // 物料描述 (对应 description 字段)
    private String reqDelivDate;      // 要求交货日期 (对应 reqDelivDate 字段)
    private String netValueStr;       // 净值字符串 (对应 netValue 字段)
    private String netValueUnit;      // 净值单位 (对应 netValueUnit 字段)
    private String taxValueStr;       // 税值字符串 (对应 taxValue 字段)
    private String taxValueUnit;      // 税值单位 (对应 taxValueUnit 字段)
    private String pricingDate;       // 定价日期 (对应 pricingDate 字段)
    private String orderProbability;  // 订单概率 (对应 orderProbability 字段)
    private String pricingElementsJson; // 定价元素JSON数据 (对应 pricingElements 字段)

    /**
     * 构造方法 - 保持向后兼容
     */
    public QuotationItemEntity(Long quotationId, Integer itemNo, Long matId, Integer quantity,
                              Float netPrice, Float itemValue, Long plantId, String su) {
        this.quotationId = quotationId;
        this.itemNo = itemNo;
        this.matId = matId;
        this.quantity = quantity;
        this.netPrice = netPrice;
        this.itemValue = itemValue;
        this.plantId = plantId;
        this.su = su;
    }
}
