package webserver.pojo;

import lombok.Data;
import java.util.List;

@Data
public class ItemValidationRequest {
    private List<Item> items;
    
    @Data
    public static class Item {
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
    }
    
    @Data
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
