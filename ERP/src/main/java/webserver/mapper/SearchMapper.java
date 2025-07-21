package webserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import webserver.pojo.SearchCondition;

import java.util.List;
import java.util.Map;

@Mapper
public interface SearchMapper {
    /**
     * 动态通用查询
     * @param table 查询表名
     * @param conditions 查询条件Map（列名->条件）
     * @param fields 查询字段列表，null或空则为 *
     * @param limit 限制条数
     * @return List<Map> 动态字段，动态结果
     */
    List<Map<String, Object>> dynamicSearch(
            @Param("table") String table,
            @Param("conditions") Map<String, SearchCondition> conditions,
            @Param("fields") List<String> fields,
            @Param("limit") Integer limit);
}
