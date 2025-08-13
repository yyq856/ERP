package webserver.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class QuotationSearchResponseDTO {
    private QuotationStruct quotationStruct;

    @Data
    public static class QuotationStruct {
        private List<QuotationNode> currentValue;
        @JsonIgnore
        private Object config;
        private boolean isEditable;
        private List<QuotationChildNode> children;
    }

    @Data
    public static class QuotationNode {
        private String salesQuotation;
        private String soldToParty;
        private String customerReference;
        private String overallStatus;
        private String latestExpiration;
    }

    @Data
    public static class QuotationChildNode {
        private String varType;
        private String nodeType;
        private String name;
        private boolean isEditable;
        private Object config;
    }
}
