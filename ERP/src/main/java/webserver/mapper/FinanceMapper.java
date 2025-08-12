package webserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import webserver.pojo.SearchOpenItemsRequest;
import java.util.List;
import java.util.Map;

@Mapper
public interface FinanceMapper {
    
    /**
     * 搜索未清项
     * @param request 搜索请求参数
     * @return 未清项列表
     */
    List<Map<String, Object>> searchOpenItems(SearchOpenItemsRequest request);
}
