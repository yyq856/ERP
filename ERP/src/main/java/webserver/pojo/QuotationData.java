package webserver.pojo;

import lombok.Data;

@Data
public class QuotationData {
    private QuotationBasicInfo basicInfo;
    private QuotationItemOverview itemOverview;
}
