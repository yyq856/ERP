package webserver.pojo;

import lombok.Data;

@Data
public class BpMaintainSearchRequest {
    private Query query;
    
    @Data
    public static class Query {
        private String customerId; // 业务伙伴ID，支持模糊查询
    }
}
