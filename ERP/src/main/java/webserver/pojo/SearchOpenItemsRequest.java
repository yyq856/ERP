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
        private String amount;  // 🔥 改为直接的字符串
        private String unit;    // 🔥 新增：货币单位

        // 🔥 兼容旧格式的Amount对象
        private Amount amountObj;

        // 🔥 获取金额的统一方法
        public String getAmountValue() {
            if (amount != null) {
                return amount;
            }
            if (amountObj != null && amountObj.getAmount() != null) {
                return amountObj.getAmount().toString();
            }
            return "0";
        }

        // 🔥 获取货币单位的统一方法
        public String getAmountUnit() {
            if (unit != null) {
                return unit;
            }
            if (amountObj != null && amountObj.getUnit() != null) {
                return amountObj.getUnit();
            }
            return "USD";
        }
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
