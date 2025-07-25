package webserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import webserver.pojo.SalesOrderSearchRequest;

import java.util.List;
import java.util.Map;

@Mapper
public interface SalesOrderMapper {
    List<Map<String, Object>> searchSalesOrders(@Param("request") SalesOrderSearchRequest request);
}
