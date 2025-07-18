package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inquiry {
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
