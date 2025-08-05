package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryInitializeRequest {
    private String inquiryType;
    private String salesOrganization;
    private String distributionChannel;
    private String division;
}
