package webserver.pojo;

import lombok.Data;

@Data
public class BpMaintainEditResponse {
    private boolean success;
    private String message;
    private BpEditData data;
    
    @Data
    public static class BpEditData {
        private String customerId; // 创建时返回新生成的ID，修改时返回原ID
    }
}
