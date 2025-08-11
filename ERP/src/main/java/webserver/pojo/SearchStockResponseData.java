package webserver.pojo;

import lombok.Data;
import java.util.List;

@Data
public class SearchStockResponseData {
    private List<StockLevelNode> content;
}