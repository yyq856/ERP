package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateItemsRequest {
    private String item;
    private String material;
    private String orderQuantity;
    private String orderQuantityUnit;
    private String description;
    private String reqDelivDate;
    private String netValue;
    private String netValueUnit;
    private String taxValue;
    private String taxValueUnit;
    private String pricingDate;
    private String orderProbability;
    private List<PricingElement> pricingElements;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingElement {
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