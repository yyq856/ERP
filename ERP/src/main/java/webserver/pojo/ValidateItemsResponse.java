package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateItemsResponse {
    private boolean success;
    private String message;
    private ValidateItemsData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidateItemsData {
        private ValidationResult result;
        private GeneralData generalData;
        private List<OutboundDeliveryItemDTO> breakdowns;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationResult {
        private Integer allDataLegal;           // 1表示所有数据合法，0表示存在不合法数据
        private List<Integer> badRecordIndices; // 不合法数据的索引列表 (从0开始)
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneralData {
        private String pickingStatus;          // 拣货状态
        private String overallStatus;          // 总体状态
        private String giStatus;               // 出库状态
        private Boolean readyToPost;           // 是否准备过账
        private String grossWeight;            // 总毛重
        private String grossWeightUnit;        // 总毛重单位
        private String netWeight;              // 总净重
        private String netWeightUnit;          // 总净重单位
        private String volume;                 // 总体积
        private String volumeUnit;             // 总体积单位
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemBreakdown {
        private String item;
        private String material;
        private String orderQuantity;
        private String orderQuantityUnit;
        private String description;
        private String reqDelivDate;
        private Double netValue;               // 计算后的净值
        private String netValueUnit;
        private Double taxValue;               // 计算后的税值
        private String taxValueUnit;
        private String pricingDate;
        private String orderProbability;
        private List<PricingElementBreakdown> pricingElements;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingElementBreakdown {
        private String cnty;
        private String name;
        private String amount;
        private String city;
        private String per;
        private String uom;
        private String conditionValue;
        private String curr;
        private String status;
        private String numC;
        private String atoMtsComponent;
        private String oun;
        private String cconDe;
        private String un;
        private String conditionValue2;
        private String cdCur;
        private Boolean stat;
    }
}
