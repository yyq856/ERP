package webserver.pojo;

import lombok.Data;

@Data
public class StockLevelData {
    private String level;
    private String qty_on_hand;
    private String qty_committed;
}
