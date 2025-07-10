package webserver.pojo;

import lombok.Data;

@Data
public class Cart {
    private String productId;
    private String imageUrl;
    private String name;
    private Double price;
    private Integer number;
    private Integer stockQuantity;


}
