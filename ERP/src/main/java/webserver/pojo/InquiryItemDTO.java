package webserver.pojo;

import lombok.Data;

@Data
public class InquiryItemDTO {
    private Long inquiryId;
    private Integer itemNo;
    private Long matId;
    private Integer quantity;
    private Float netPrice;
    private Float itemValue;
    private Long plantId;
    private String su;
}
