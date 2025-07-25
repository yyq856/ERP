package webserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import webserver.pojo.SalesOrderSearchRequest;

import java.util.List;
import java.util.Map;

@Mapper
public interface SalesOrderMapper {
    List<Map<String, Object>> searchSalesOrders(@Param("request") SalesOrderSearchRequest request);
    
    // ✅ 新增方法：获取销售订单详情
    Map<String, Object> getSalesOrderDetails(@Param("soId") String soId);
    
    // ✅ 新增方法：获取销售订单项目列表
    List<Map<String, Object>> getSalesOrderItems(@Param("soId") String soId);
}
