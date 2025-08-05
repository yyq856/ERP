package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryResponse {
    private Boolean success;
    private String message;
    private Object data;
    
    // 成功响应
    public static InquiryResponse success(Object data, String message) {
        return new InquiryResponse(true, message, data);
    }
    
    // 失败响应
    public static InquiryResponse error(String message) {
        return new InquiryResponse(false, message, null);
    }
}
