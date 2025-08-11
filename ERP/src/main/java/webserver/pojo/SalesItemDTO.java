package webserver.pojo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SalesItemDTO {
    private Integer itemNo;
    private Long matId;
    private Integer pickQuantity;
    private Long plantId;
    private String storageLoc;
}
