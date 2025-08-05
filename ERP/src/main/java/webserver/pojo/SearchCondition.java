package webserver.pojo;

import lombok.Data;



@Data
public class SearchCondition {
    private String regex;
    private Object gte;
    private Object lte;
    private Object eq;
}
