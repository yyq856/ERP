package webserver.pojo;

import lombok.Data;

import java.util.List;

@Data
public class QuotationItemOverview {
    private String validFrom;              // 日期
    private String validTo;                // 日期
    private String reqDelivDate;           // 日期
    private String expectedOralVal;
    private String expectedOralValUnit;
    private List<QuotationItem> items;
}
