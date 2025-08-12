package webserver.pojo;

import lombok.Data;
import java.util.List;

@Data
public class BillingEditRequest {
    private Meta meta;
    private BasicInfo basicInfo;
    private ItemOverview itemOverview;
    
    @Data
    public static class Meta {
        private String id;
    }
    
    @Data
    public static class BasicInfo {
        private String type;
        private String id;
        private String netValue;
        private String netValueUnit;
        private String payer;
        private String payerId;
        private String billingDate;
        private String taxValue;
        private String grossValue;
    }
    
    @Data
    public static class ItemOverview {
        private List<Item> items;
    }
    
    @Data
    public static class Item {
        private String item;
        private String itemNo;
        private String material;
        private String materialId;
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
        private String dlvId;
        private String quantity;
        private String netPrice;
        private String taxRate;
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
