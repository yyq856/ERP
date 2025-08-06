package webserver.pojo;

import lombok.Data;

@Data
public class OutboundDeliverySummaryDTO {
    private String outboundDelivery;  // eg: DEL-2024-100
    private String pickingDate;
    private String pickingStatus;
    private String giStatus;
    private String pick; // 一般前端按钮的文案
}
