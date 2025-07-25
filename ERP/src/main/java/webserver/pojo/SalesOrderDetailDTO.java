package webserver.pojo;


import lombok.Data;
import java.util.List;

@Data
public class SalesOrderDetailDTO {
    private Meta meta = new Meta();
    private BasicInfo basicInfo = new BasicInfo();
    private ItemOverview itemOverview = new ItemOverview();

    @Data
    public static class Meta {
        private String id;
    }

    @Data
    public static class BasicInfo {
        private String quotation_id;
        private String so_id;
        private String soldToParty;
        private String shipToParty;
        private String customerReference;
        private String netValue;
        private String netValueUnit;
        private String customerReferenceDate;
    }

    @Data
    public static class ItemOverview {
        private String reqDelivDate;
        private List<Item> items = List.of();
    }

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
    }
}
