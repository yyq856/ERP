package webserver.pojo;

import lombok.Data;
import java.util.List;

@Data
public class PostGIsByIdRequest {
    private List<String> deliveryIds;
}
