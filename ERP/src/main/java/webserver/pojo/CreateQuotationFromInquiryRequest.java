package webserver.pojo;

import lombok.Data;

@Data
public class CreateQuotationFromInquiryRequest {
    private String inquiryId;
    private String customerId;
}
