package webserver.pojo;

import lombok.Data;

@Data
public class QuotationBasicInfo {
    private String quotation;              // 报价单id
    private String soldToParty;
    private String shipToParty;
    private String customerReference;
    private Float netValue;
    private String netValueUnit;
    private String customerReferenceDate; // yyyy-MM-dd 格式
}
