package webserver.service;


import webserver.pojo.*;

import java.util.List;
import java.util.Map;

public interface SearchService {
    List<Map<String, Object>> search(SearchRequest request);
}
