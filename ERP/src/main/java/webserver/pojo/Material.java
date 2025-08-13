package webserver.pojo;

import lombok.Data;

@Data
public class Material {
    private Long matId;       // mat_id
    private String matDesc;   // mat_desc
    private String baseUom;   // base_uom
    private Double srdPrice;  // srd_price (standard/base price per unit)
}

