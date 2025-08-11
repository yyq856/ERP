package webserver.pojo;

import lombok.Data;

import java.util.List;

@Data
public class QuotationDetailsResponse {

    private Meta meta;
    private BasicInfo basicInfo;
    private ItemOverview itemOverview;

    @Data
    public static class Meta {
        private String id;
    }

    @Data
    public static class BasicInfo {
        private String inquiry;               // 询价单号（可能为null）
        private String quotation;             // 报价单号
        private String soldToParty;           // 售达方
        private String shipToParty;           // 送达方
        private String customerReference;     // 客户参考号
        private String netValue;              // 净值字符串
        private String netValueUnit;          // 净值单位，如"USD"
        private String customerReferenceDate; // 客户参考日期 (YYYY-MM-DD)
    }

    @Data
    public static class ItemOverview {
        private String validFrom;
        private String validTo;
        private String reqDelivDate;
        private String expectOralVal;
        private String expectOralValUnit;
        private List<Item> items;
    }

    @Data
    public static class Item {
        private String material;      // 物料id字符串
        private String orderQuantity; // 字符串，数量
        private Integer su;           // 单位数量
        private Integer altItm;       // 备用标记
        private String description;   // 物料描述
    }
}
