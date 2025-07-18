package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class InquiryItemsTabQueryRequest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemQuery {
        private String item;
        private String material;
        private String orderQuantity;
        private String orderQuantityUnit;
        private String description;
        private String reqDelivDate;
        private String netValue;
        private String pricingDate;
        private String orderProbability;
    }
}
