package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BpRelationshipResponse {
    private Boolean success;
    private String message;
    private Object data;
    
    // 成功响应
    public static BpRelationshipResponse success(Object data, String message) {
        return new BpRelationshipResponse(true, message, data);
    }
    
    // 失败响应
    public static BpRelationshipResponse error(String message) {
        return new BpRelationshipResponse(false, message, null);
    }
}
