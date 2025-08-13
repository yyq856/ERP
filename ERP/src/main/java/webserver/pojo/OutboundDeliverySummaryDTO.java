package webserver.pojo;

import lombok.Data;

@Data
public class OutboundDeliverySummaryDTO {
    private String outboundDelivery; // 出库交货单ID
    private String pickingDate = ""; // 默认空字符串
    private String pickingStatus = "Not Started"; // 默认
    private String giStatus = "Not Started"; // 默认
}
