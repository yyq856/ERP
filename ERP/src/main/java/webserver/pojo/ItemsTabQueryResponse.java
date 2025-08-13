package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 物品条件查询响应类
 * 用于支持items-tab-query接口的响应数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemsTabQueryResponse {
    private boolean success;
    private String message;
    private ItemsTabQueryData data;
    
    /**
     * 响应数据主体
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemsTabQueryData {
        private ValidationResult result;        // 验证结果
        private GeneralData generalData;        // 总体数据
        private List<ItemBreakdown> breakdowns; // 物品明细列表
    }
    
    /**
     * 验证结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationResult {
        private Integer allDataLegal;           // 1表示所有数据合法，0表示存在不合法数据
        private List<Integer> badRecordIndices; // 不合法数据的索引列表 (从0开始)
    }
    
    /**
     * 总体数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneralData {
        private String netValue;               // 总净值
        private String netValueUnit;           // 总净值单位
        private String expectOralVal;          // 总预期口头值
        private String expectOralValUnit;      // 总预期口头值单位
    }
    
    /**
     * 物品明细
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemBreakdown {
        private String item;                   // 行号-按顺序生成
        private String material;               // 材料的名称
        private String orderQuantity;          // 数量
        private String orderQuantityUnit;      // 单位-由材料生成
        private String description;            // 描述-由材料生成
        private String reqDelivDate;           // 送货日期，设定一个默认值 或者 返回用户输入的值
        private Double netValue;               // 根据pricingElement计算后的净值
        private String netValueUnit;           // 币种，根据pricingElement
        private Double taxValue;               // 计算后的税值，netValue * 0.13
        private String taxValueUnit;           // 同netValueUnit
        private String pricingDate;            // 日期，设定一个默认值 或者 返回用户输入的值
        private String orderProbability;       // 默认值，100或者 返回用户输入的值
        private List<PricingElementBreakdown> pricingElements; // 定价元素列表
    }
    
    /**
     * 定价元素明细
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingElementBreakdown {
        private String cnty;                   // 定价元素的唯一标识，例如：BASE（基础价格），DISC（固定折扣），DISCP（百分比折扣）
        private String name;                   // 定价元素的文字说明，如：基础价格，现金折扣，百分比折扣
        private String amount;                 // 该定价元素的金额。可以是货币值（如 $10.00）或百分比（如 10%）
        private String city;                   // 应该改名叫crcy，上一行对应的单位，比如打折那就是用商品的单位，百分比打折那就是用%
        private String per;                    // 指示上面的金额是每单位的数量。例如每两件，那就是2。默认是1，用户可以改
        private String uom;                    // 上一行数量的单位。例如每两件，那就是EA（每件）。默认是EA，用户可以改
        private String conditionValue;         // 本行定价元素的金额，参与最后的netValue计算，例如打折造成了减25，那就是-25。后端计算得到
        private String curr;                   // 金额的币种单位，和material的单位是一样的，由material生成
        private String status;                 // 状态，留空
        private String numC;                   // 数量条件，留空
        private String atoMtsComponent;        // ATO/MTS组件标识，留空
        private String oun;                    // OUn字段，留空
        private String cconDe;                 // CConDe字段，留空
        private String un;                     // Un字段，留空
        private String conditionValue2;        // 条件值2，留空
        private String cdCur;                  // CdCur字段，留空
        private Boolean stat;                  // 统计标志，默认是true
    }
}