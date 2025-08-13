package webserver.pojo;

import lombok.Data;

@Data
public class StockRecord {
    private Long bpId;
    private Long plantId;
    private Double qtyOnHand;
    private Double qtyCommitted;
}
