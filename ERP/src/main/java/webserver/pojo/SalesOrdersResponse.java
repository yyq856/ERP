package webserver.pojo;

import lombok.Data;
import java.util.List;

@Data
public class SalesOrdersResponse {
    private boolean success;
    private String message;
    private DataContent data;

    @Data
    public static class DataContent {
        private List<OrderSummary> orders;
    }
}