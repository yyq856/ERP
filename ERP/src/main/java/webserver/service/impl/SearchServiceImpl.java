package webserver.service.impl;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webserver.mapper.SearchMapper;
import webserver.pojo.SearchCondition;
import webserver.pojo.SearchRequest;
import webserver.service.SearchService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private SearchMapper searchMapper;

    @Override
    public List<Map<String, Object>> search(SearchRequest request) {
        String table = request.getTableName();
        Map<String, SearchCondition> conditions = request.getConditions();
        List<String> fields = request.getRequireOutputField();
        Integer limit = request.getLimit();

        // 空值处理
        if (conditions == null) {
            conditions = Collections.emptyMap();
        }
        if (fields == null || fields.isEmpty()) {
            fields = Collections.singletonList("*");
        }

        System.out.println("Table: " + table);
        System.out.println("Conditions: " + conditions);
        System.out.println("Fields: " + fields);
        System.out.println("Limit: " + limit);

        return searchMapper.dynamicSearch(table, conditions, fields, limit);
    }
}
