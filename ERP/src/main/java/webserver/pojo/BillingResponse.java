package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 开票凭证通用响应类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillingResponse {
    private boolean success;
    private String message;
    private Object data;

    public static BillingResponse success(Object data, String message) {
        return new BillingResponse(true, message, data);
    }

    public static BillingResponse error(String message) {
        return new BillingResponse(false, message, null);
    }
}
