package webserver.pojo;

import lombok.Data;

import java.util.Map;

@Data
public class SearchCondition {
    private String regex;
    private Object gte;
    private Object lte;
    private Object eq;
}
