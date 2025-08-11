package webserver.pojo;

import lombok.Data;

@Data
public class StockLevelNode {
    private StockLevelData data;
    private int depth;   // 0,1 代表层级深度
}
