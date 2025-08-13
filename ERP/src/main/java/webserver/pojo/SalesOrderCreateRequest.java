package webserver.pojo;

import java.util.List;
import lombok.Data;


@Data
public class SalesOrderCreateRequest {
    private Meta meta;
    private BasicInfo basicInfo;
    private ItemOverview itemOverview;

    // Getters and Setters
    @Data
    public static class Meta {
        private String id;
        // Getter and Setter
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
        
        // 新增字段
        private String quotation; // 报价单号
        private String currency; // 货币代码
        private String taxValue; // 税值
        private String taxValueUnit; // 税值单位
        private String grossValue; // 总值
        private String grossValueUnit; // 总值单位
        
        // 添加salesQuotationId字段
        private String salesQuotationId;
        
        // Getters and Setters for salesQuotationId
        public String getSalesQuotationId() {
            return salesQuotationId;
        }
        
        public void setSalesQuotationId(String salesQuotationId) {
            this.salesQuotationId = salesQuotationId;
        }
    }

    @Data
    public static class ItemOverview {
        private String reqDelivDate;
        private List<Item> items;
        // Getters and Setters
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
        private List<PricingElement> pricingElements;
        // Getters and Setters
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
