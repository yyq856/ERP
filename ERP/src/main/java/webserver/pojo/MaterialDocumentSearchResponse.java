package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDocumentSearchResponse {
    private boolean success;
    private String message;
    private List<MaterialDocumentSummary> data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialDocumentSummary {
        private String materialDocument;      // 物料凭证号码
        private String plant;                 // 工厂名称
        private String postingDate;           // 过账日期 (ISO 8601格式)
        private String documentDate;          // 凭证日期 (ISO 8601格式)
        private Long materialDocumentId;      // 物料凭证ID (用于详情查询)
    }
}
