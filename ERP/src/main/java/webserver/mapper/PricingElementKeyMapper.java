package webserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import webserver.entity.PricingElementKey;

import java.util.List;
import java.util.Map;

/**
 * 定价元素类型配置 Mapper
 */
@Mapper
public interface PricingElementKeyMapper {
    
    /**
     * 根据ID查询定价元素类型
     * @param id 主键ID
     * @return 定价元素类型
     */
    PricingElementKey selectById(@Param("id") Integer id);
    
    /**
     * 根据名称查询定价元素类型
     * @param name 名称代码
     * @return 定价元素类型
     */
    PricingElementKey selectByName(@Param("name") String name);
    
    /**
     * 查询所有定价元素类型
     * @return 定价元素类型列表
     */
    List<PricingElementKey> selectAll();
    
    /**
     * 查询所有定价元素类型，返回Map格式（name -> PricingElementKey）
     * @return 定价元素类型Map
     */
    Map<String, PricingElementKey> selectAllAsMap();
    
    /**
     * 插入新的定价元素类型
     * @param pricingElementKey 定价元素类型
     * @return 影响行数
     */
    int insert(PricingElementKey pricingElementKey);
    
    /**
     * 更新定价元素类型
     * @param pricingElementKey 定价元素类型
     * @return 影响行数
     */
    int update(PricingElementKey pricingElementKey);
    
    /**
     * 根据ID删除定价元素类型
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Integer id);
    
    /**
     * 根据名称删除定价元素类型
     * @param name 名称代码
     * @return 影响行数
     */
    int deleteByName(@Param("name") String name);
}
