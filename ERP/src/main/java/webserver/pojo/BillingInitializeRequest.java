package webserver.pojo;

import lombok.Data;

@Data
public class BillingInitializeRequest {
    private BillingDueList billingDueList;
    
    @Data
    public static class BillingDueList {
        private String billingDate;
        private String soldToParty;
    }
}
