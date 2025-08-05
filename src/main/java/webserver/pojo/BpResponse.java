package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BpResponse {
    private Boolean success;
    private Object data;
    private String message;
    private String error;

    // 成功响应
    public static BpResponse success(Object data, String message) {
        return new BpResponse(true, data, message, null);
    }

    // 成功响应（无数据）
    public static BpResponse success(String message) {
        return new BpResponse(true, null, message, null);
    }

    // 失败响应
    public static BpResponse error(String error, String message) {
        return new BpResponse(false, null, message, error);
    }
}
