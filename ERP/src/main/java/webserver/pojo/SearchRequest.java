package webserver.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SearchRequest {
    /**
     * 查询的表名（必须是数据库中的表名）
     */
    @JsonProperty("table")
    private String tableName;

    /**
     * 查询条件，key是列名，value是值或条件对象
     * 示例：
     * {
     *   "city": { "regex": "^北京.*" },
     *   "credit": { "gte": 1000, "lte": 5000 }
     * }
     */
    @JsonProperty("filters")
    private Map<String, SearchCondition> conditions;


    /**
     * 要返回的字段列表，如为空则默认全部返回
     */
    @JsonProperty("requireOutputFields")
    private List<String> requireOutputField;

    /**
     * 返回结果最大条数限制
     */
    private Integer limit;
}
