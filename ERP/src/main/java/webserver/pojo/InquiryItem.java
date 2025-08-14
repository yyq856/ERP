package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 询价单项目实体类
 * 扩展支持完整的 ItemValidation 字段结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryItem {
    // 原有字段
    private Long inquiryId;           // 询价单ID
    private Integer itemNo;           // 项目号
    private Long matId;               // 物料ID (数字形式，用于数据库关联)
    private Integer quantity;         // 数量 (数字形式)
    private Float netPrice;           // 净价 (数字形式)
    private Float itemValue;          // 项目总值
    private Long plantId;             // 工厂ID
    private String su;                // 单位
    
    // 新增字段 - 对应 ItemValidationRequest 的完整字段结构
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
    public InquiryItem(Long inquiryId, Integer itemNo, Long matId, Integer quantity, 
                       Float netPrice, Float itemValue, Long plantId, String su) {
        this.inquiryId = inquiryId;
        this.itemNo = itemNo;
        this.matId = matId;
        this.quantity = quantity;
        this.netPrice = netPrice;
        this.itemValue = itemValue;
        this.plantId = plantId;
        this.su = su;
    }
}
