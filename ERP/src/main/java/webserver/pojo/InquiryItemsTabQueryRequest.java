package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 询价单物品条件查询请求类
 * 扩展支持完整的 ItemValidation 字段结构
 */
public class InquiryItemsTabQueryRequest {

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
        private List<PricingElementRequest> pricingElements; // 定价元素列表
    }

    /**
     * 定价元素请求结构
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingElementRequest {
        private String cnty;                    // 定价元素唯一标识
        private String name;                    // 定价元素文字说明
        private String amount;                  // 金额
        private String city;                    // 货币单位（应该叫crcy）
        private String per;                     // 每单位数量
        private String uom;                     // 计量单位
        private String conditionValue;          // 条件值
        private String curr;                    // 货币代码
        private String status;                  // 状态
        private String numC;                    // 数量条件
        private String atoMtsComponent;         // ATO/MTS组件标识
        private String oun;                     // OUn字段
        private String cconDe;                  // CConDe字段
        private String un;                      // Un字段
        private String conditionValue2;         // 条件值2
        private String cdCur;                   // CD货币
        private Boolean stat;                   // 状态标志
    }
}
