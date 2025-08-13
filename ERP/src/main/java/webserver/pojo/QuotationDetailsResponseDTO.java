package webserver.pojo;

import lombok.Data;
import java.util.List;

@Data
public class QuotationDetailsResponseDTO {
    private Meta meta;
    private BasicInfo basicInfo;
    private ItemOverview itemOverview;

    @Data
    public static class Meta {
        private String id;
    }

    @Data
    public static class BasicInfo {
        private String inquiry;              // 询价单号
        private String quotation;            // 报价单号
        private String soldToParty;          // 售达方
        private String shipToParty;          // 送达方
        private String customerReference;    // 客户参考
        private float netValue;              // 净值
        private String netValueUnit;         // 净值单位
        private String customerReferenceDate;// 客户参考日期
    }

    @Data
    public static class ItemOverview {
        private String validFrom;
        private String validTo;
        private String reqDelivDate;
        private String expectedOralVal;
        private String expectedOralValUnit;
        private List<QuotationItemDTO> items;
    }
}
