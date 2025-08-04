package webserver.pojo;

import lombok.Data;
import java.util.List;

@Data
public class CreateOutboundDeliveryResponseData {
    private String message;
    private List<String> createdDeliveries;
}
