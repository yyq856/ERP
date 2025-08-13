package webserver.service;

import webserver.pojo.SearchOpenItemsRequest;
import java.util.Map;

public interface FinanceService {
    
    /**
     * 搜索未清项
     * @param request 搜索请求参数
     * @return 搜索结果
     */
    Map<String, Object> searchOpenItems(SearchOpenItemsRequest request);
}
