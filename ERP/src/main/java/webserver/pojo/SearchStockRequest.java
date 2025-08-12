package webserver.pojo;

import lombok.Data;

@Data
public class SearchStockRequest {
    private String id;  // 材料ID，注意类型与数据库保持一致
}
