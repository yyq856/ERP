package webserver.pojo;

import lombok.Data;

@Data
public class SearchOpenItemsRequest {
    private GeneralInformation generalInformation;
    private BankDate bankDate;
    private OpenItemSelection openItemSelection;
    
    @Data
    public static class GeneralInformation {
        private String companyCode;
        private String journalEntryDate;
        private String journalEntryType;
        private String period;
        private String postingDate;
    }
    
    @Data
    public static class BankDate {
        private String glAccount;
        private Amount amount;
    }
    
    @Data
    public static class Amount {
        private Float amount;
        private String unit;
    }
    
    @Data
    public static class OpenItemSelection {
        private Float accountID;
        private String accountType; // 只有customer和vendor
    }
}
