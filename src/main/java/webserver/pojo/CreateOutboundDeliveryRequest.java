package webserver.pojo;

import lombok.Data;
import java.util.List;

@Data
public class CreateOutboundDeliveryRequest {
    private List<SalesOrderIdDTO> selectedOrders;

    @Data
    public static class SalesOrderIdDTO {
        private String id;
    }
}
