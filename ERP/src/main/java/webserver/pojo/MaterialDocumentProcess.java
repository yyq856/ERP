package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDocumentProcess {
    private Long materialDocumentId;
    private Long dlvId;
    private Long billId;
    private Long soId;
}
