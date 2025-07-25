package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderSearchResult {
    private String soId;
    private String soldToPartyName;
    private String customerNo;
    private String customerReference;
    private String reqDeliveryDate;
    private String status;
    private String netValue;
    private String currency;
    private String docDate;
}
