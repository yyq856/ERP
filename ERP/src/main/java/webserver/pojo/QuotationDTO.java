package webserver.pojo;

import lombok.Data;
import java.util.Date;

@Data
public class QuotationDTO {
    private Long quotationId;
    private Long referenceInquiryId;
    private Long custId;
    private String inquiryType;
    private String slsOrg;
    private String salesDistrict;
    private String division;
    private Long soldTp;
    private Long shipTp;
    private String custRef;
    private Date customerReferenceDate;
    private Date validFromDate;
    private Date validToDate;
    private Float probability;
    private Float netValue;
    private String status;
    private String currency;
}
