package webserver.pojo;

import lombok.Data;
import java.util.List;

@Data
public class BpMaintainSearchResponse {
    private boolean success;
    private String message;
    private List<BpSearchItem> data;
    
    @Data
    public static class BpSearchItem {
        private String customerId;    // 业务伙伴ID
        private String name;          // org和group类型名称
        private String firstName;     // person类型名字
        private String lastName;      // person类型姓氏
        private String city;          // 城市
        private String country;       // 国家
        private String bpRole;        // BP角色
        private String type;          // 客户类型：person/org/group
    }
}
