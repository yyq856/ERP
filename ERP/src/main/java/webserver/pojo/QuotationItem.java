package webserver.pojo;

import lombok.Data;

@Data
public class QuotationItem {
    private String item;                   // item id
    private String material;
    private String orderQuantity;
    private String orderQuantityUnit;
    private String description;
    private Integer su;
    private Integer altItm;
}
