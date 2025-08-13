package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 物品条件查询请求类
 * 用于支持items-tab-query接口的请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemsTabQueryRequest {
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
    private List<PricingElementRequest> pricingElements; // 定价元素列表
    
    /**
     * 定价元素请求结构
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingElementRequest {
        private String cnty;                    // 国家代码/条件类型
        private String name;                    // 条件名称 (例如: "Base Price", "Tax", "Discount")
        private String amount;                  // 金额
        private String city;                    // 城市/货币标识
        private String per;                     // 每 (例如: "1")
        private String uom;                     // 计量单位 (例如: "EA", "KG")
        private String conditionValue;          // 条件值
        private String curr;                    // 货币代码 (例如: "USD", "EUR")
        private String status;                  // 状态 (例如: "Active", "Inactive")
        private String numC;                    // 数量条件
        private String atoMtsComponent;         // ATO/MTS组件标识
        private String oun;                     // OUn字段
        private String cconDe;                  // CConDe字段
        private String un;                      // Un字段
        private String conditionValue2;         // 条件值2
        private String cdCur;                   // CdCur字段
        private Boolean stat;                   // 统计标志
    }
}