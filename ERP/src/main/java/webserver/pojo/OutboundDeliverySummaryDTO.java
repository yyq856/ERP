package webserver.pojo;

import lombok.Data;

@Data
public class OutboundDeliverySummaryDTO {
    private String outboundDelivery;
    private String pickingDate;
    private String pickingStatus;
    private String giStatus;
}
