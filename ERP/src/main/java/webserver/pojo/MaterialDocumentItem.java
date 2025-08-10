package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDocumentItem {
    private Long materialDocumentId;
    private Integer itemNo;
    private Long matId;
    private String materialDesc;
    private BigDecimal quantity;
    private String unit;  // 映射数据库字段 unit
    private String movementType;
    private String storageLoc;
}
