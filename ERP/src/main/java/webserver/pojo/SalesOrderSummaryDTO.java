package webserver.pojo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SalesOrderSummaryDTO {
    private Long soId;
    private String customerName;
    private String contactName;
    private LocalDate reqDeliveryDate;
    private Double grossValue;
    private String plannedCreationDate;
    private String plannedGIDate;
    private String shippingPoint;
    private String shipToParty;
    private String grossWeight;
}