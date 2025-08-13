package webserver.pojo;

import lombok.Data;

@Data
public class BillingSearchRequest {
    private String billingDocument;
    private String billingDate;
    private String payer;
}
