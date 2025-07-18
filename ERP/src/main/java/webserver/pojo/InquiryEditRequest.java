package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryEditRequest {
    private Meta meta;
    private BasicInfo basicInfo;
    private ItemOverview itemOverview;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private String id;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BasicInfo {
        private String inquiry;
        private String soldToParty;
        private String shipToParty;
        private String customerReference;
        private Double netValue;
        private String netValueUnit;
        private String customerReferenceDate;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemOverview {
        private String validFrom;
        private String validTo;
        private String reqDelivDate;
        private String expectOralVal;
        private String expectOralValUnit;
        private List<InquiryItemDetail> items;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InquiryItemDetail {
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
