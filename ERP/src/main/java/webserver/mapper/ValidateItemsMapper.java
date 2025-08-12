package webserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface ValidateItemsMapper {
    
    /**
     * 检查物料是否存在
     * @param materialId 物料ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM erp_material WHERE mat_id = #{materialId}")
    boolean materialExists(@Param("materialId") Long materialId);
    
    /**
     * 获取物料的标准价格
     * @param materialId 物料ID
     * @return 标准价格
     */
    @Select("SELECT srd_price FROM erp_material WHERE mat_id = #{materialId}")
    BigDecimal getMaterialStandardPrice(@Param("materialId") Long materialId);
    
    /**
     * 获取物料描述信息
     * @param materialId 物料ID
     * @return 物料描述
     */
    @Select("SELECT mat_desc FROM erp_material WHERE mat_id = #{materialId}")
    String getMaterialDescription(@Param("materialId") Long materialId);
    
    /**
     * 获取物料基本单位
     * @param materialId 物料ID
     * @return 基本单位
     */
    @Select("SELECT base_uom FROM erp_material WHERE mat_id = #{materialId}")
    String getMaterialBaseUnit(@Param("materialId") Long materialId);
}