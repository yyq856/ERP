package webserver.pojo;

import lombok.Data;

@Data
public class QuotationItemEntity {
    private Long quotationId;
    private Integer itemNo;
    private Long matId;
    private Integer quantity;
    private Float netPrice;
    private Integer itemDiscountPct;
    private Float itemValue;
    private Long plantId;
    private String su;
    private Integer cnty;
    private Float taxValue;
}
