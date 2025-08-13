package webserver.pojo;

import lombok.Data;
import java.util.List;

@Data
public class QuotationResponseDTO {
    private Meta meta;
    private BasicInfo basicInfo;
    private ItemOverview itemOverview;

    @Data
    public static class Meta {
        private String id;
    }

    @Data
    public static class BasicInfo {
        private String quotation;
        private String soldToParty;
        private String shipToParty;
        private String customerReference;
        private float netValue;
        private String netValueUnit;
        private String customerReferenceDate;
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
