package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDocument {
    private Long materialDocumentId;
    private String materialDocument;
    private String materialDocumentYear;
    private Long plantId;
    private String plantName;
    private LocalDate postingDate;
    private LocalDate documentDate;
    private String createdBy;
    
    // 物料凭证项目列表
    private List<MaterialDocumentItem> items;
    
    // 业务流程关联信息
    private MaterialDocumentProcess processFlow;
}
