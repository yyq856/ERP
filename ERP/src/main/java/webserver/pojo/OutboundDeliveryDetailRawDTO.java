package webserver.pojo;

import lombok.Data;

@Data
public class OutboundDeliveryDetailRawDTO {
    private String id;              // 对应 dlv_id AS id
    private String plannedGIDate;
    private String actualGIDate;
    private String soId;
    private String shipToParty;     // 原 ship_tp
    private String shippingPoint;
    private String pickingStatus;
    private String giStatus;
}
