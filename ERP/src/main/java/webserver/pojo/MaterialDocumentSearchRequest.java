package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDocumentSearchRequest {
    private String materialDocument;      // 物料凭证号码 (可选)
    private String plant;                 // 工厂 (可选)
    private String materialDocumentYear;  // 物料凭证年份 (可选)
    private String material;              // 物料 (可选)
    private String postingDate;           // 过账日期 (ISO 8601格式, 可选)
    private String documentDate;          // 凭证日期 (ISO 8601格式, 可选)
}
