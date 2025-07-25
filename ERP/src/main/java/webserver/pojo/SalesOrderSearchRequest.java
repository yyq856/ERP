package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderSearchRequest {
    private String soId;
    private String status;
    private String customerNo;
    private String customerReference;
}

