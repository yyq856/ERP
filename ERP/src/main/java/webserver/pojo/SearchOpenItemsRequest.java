package webserver.pojo;

import lombok.Data;

@Data
public class SearchOpenItemsRequest {
    private GeneralInformation generalInformation;
    private BankData bankData;
    private OpenItemSelection openItemSelection;
    
    @Data
    public static class GeneralInformation {
        private String customerID;      // 🔥 新增：客户ID
        private String companyCode;
        private String journalEntryDate;
        private String journalEntryType;
        private String period;
        private String postingDate;
    }
    
    @Data
    public static class BankData {
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
        private Object accountID;  // 使用Object类型以兼容字符串和数字
        private String accountType; // 只有customer和vendor
    }
}
