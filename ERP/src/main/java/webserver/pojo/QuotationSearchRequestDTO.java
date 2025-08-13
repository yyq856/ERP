package webserver.pojo;

import lombok.Data;

@Data
public class QuotationSearchRequestDTO {
    private String customerReference;
    private String latestExpiration; // YYYY-MM-DD
    private String overallStatus;
    private String salesQuotation;
    private String soldToParty;
}