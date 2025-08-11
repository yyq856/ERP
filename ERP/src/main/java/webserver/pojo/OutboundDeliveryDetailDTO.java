package webserver.pojo;

import lombok.Data;

import java.util.List;

@Data
public class OutboundDeliveryDetailDTO {
    private String id;
    private String shipToParty;
    private String pickingStatus;
    private String giStatus;
    private String actualGIDate;
    private String plannedGIDate;
    private String deliveryDate;
    private List<OutboundDeliveryItemDTO> items;
}
