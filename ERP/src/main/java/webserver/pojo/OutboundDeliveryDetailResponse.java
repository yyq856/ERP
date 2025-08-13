package webserver.pojo;

import lombok.Data;

@Data
public class OutboundDeliveryDetailResponse {
    private OutboundDeliveryDetailDTO detail;
    private OutboundDeliveryDetailDTO.ItemsWrapper items;
}