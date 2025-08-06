package webserver.pojo;

import lombok.Data;
import java.util.List;

@Data
public class PostGIsRequest {
    private OutboundDeliveryDetailDTO deliveryDetail;
    private List<OutboundDeliveryItemDTO> items;
}
