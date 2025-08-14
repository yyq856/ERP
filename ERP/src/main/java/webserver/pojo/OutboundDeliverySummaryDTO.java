package webserver.pojo;

import lombok.Data;

@Data
public class OutboundDeliverySummaryDTO {
    private String outboundDelivery;
    private String pickingDate = "";
    private String pickingStatus = "Not Started"; // 默认
    private String giStatus = "Not Started"; // 默认
}
