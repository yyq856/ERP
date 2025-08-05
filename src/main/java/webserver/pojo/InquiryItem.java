package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryItem {
    private Long inquiryId;
    private Integer itemNo;
    private Long matId;
    private Integer quantity;
    private Float netPrice;
    private Float itemValue;
    private Long plantId;
    private String su;
}
