package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 销售订单通用响应类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderResponse {
    private boolean success;
    private String message;
    private Object data;

    public static SalesOrderResponse success(Object data, String message) {
        return new SalesOrderResponse(true, message, data);
    }

    public static SalesOrderResponse error(String message) {
        return new SalesOrderResponse(false, message, null);
    }
}
