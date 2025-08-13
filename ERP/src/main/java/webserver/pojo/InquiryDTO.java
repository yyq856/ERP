package webserver.pojo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class InquiryDTO {
    private Long inquiryId;
    private Long custId;
    private String inquiryType;
    private String slsOrg;
    private String salesDistrict;
    private String division;
    private Long soldTp;
    private Long shipTp;
    private String custRef;
    private LocalDate customerReferenceDate;
    private LocalDate validFromDate;
    private LocalDate validToDate;
    private Float probability;
    private Float netValue;
    private String status;
}
