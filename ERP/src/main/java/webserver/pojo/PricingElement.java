package webserver.pojo;

import lombok.Data;

@Data
public class PricingElement {
    private Long elementId;
    private String documentType;
    private Long documentId;
    private Integer itemNo;
    private String cnty;
    private String conditionName;
    private String amount;
    private String city;
    private String perValue;
    private String uom;
    private String conditionValue;
    private String currency;
    private String status;
    private String numC;
    private String atoMtsComponent;
    private String oun;
    private String cconDe;
    private String un;
    private String conditionValue2;
    private String cdCur;
    private Boolean stat;
}
