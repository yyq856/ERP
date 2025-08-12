package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockContentItem {
    private Map<String, String> data; // 例如 {"level":"Full", "stage0":"130", ...}
    private int depth;
}
