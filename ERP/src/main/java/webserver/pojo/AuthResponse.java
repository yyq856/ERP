package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Boolean success;
    private String message;
    private String token;
    private Integer statusCode;
    private String errorCode;
    private Object details;
    private Object data;

    // 成功响应（注册）
    public static AuthResponse successRegister(String message) {
        return new AuthResponse(true, message, null, 200, null, null, null);
    }

    // 成功响应（登录）
    public static AuthResponse successLogin(String token, String message, Object userData) {
        return new AuthResponse(true, message, token, 200, null, null, userData);
    }

    // 失败响应
    public static AuthResponse error(String message, Integer statusCode, String errorCode, Object details) {
        return new AuthResponse(false, message, null, statusCode, errorCode, details, null);
    }

    // 失败响应（简化版）
    public static AuthResponse error(String message, Integer statusCode, String errorCode) {
        return new AuthResponse(false, message, null, statusCode, errorCode, null, null);
    }
}
