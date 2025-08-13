package webserver.pojo;

import lombok.Data;

@Data
public class QuotationItemDTO {
    private String item;
    private String material;
    private String orderQuantity;
    private String orderQuantityUnit;
    private String description;
    private String su;
    private int altItm;
    private Float netPrice;
}
