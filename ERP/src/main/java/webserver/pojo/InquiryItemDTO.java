package webserver.pojo;

import lombok.Data;

@Data
public class InquiryItemDTO {
    // 基础字段
    private Long inquiryId;
    private Integer itemNo;
    private Long matId;
    private Integer quantity;
    private Float netPrice;
    private Float itemValue;
    private Long plantId;
    private String su;

    // 扩展字段 - 支持完整的ItemValidation结构
    private String itemCode;          // 项目代码
    private String materialCode;      // 物料代码
    private String orderQuantityStr;  // 订单数量字符串
    private String orderQuantityUnit; // 订单数量单位
    private String description;       // 物料描述
    private String reqDelivDate;      // 要求交货日期
    private String netValueStr;       // 净值字符串
    private String netValueUnit;      // 净值单位
    private String taxValueStr;       // 税值字符串
    private String taxValueUnit;      // 税值单位
    private String pricingDate;       // 定价日期
    private String orderProbability;  // 订单概率
    private String pricingElementsJson; // 定价元素JSON数据
}
