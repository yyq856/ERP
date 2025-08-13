package webserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import webserver.pojo.PricingConditionType;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ValidateItemsMapper {
    
    /**
     * 检查物料是否存在
     * @param materialId 物料ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM erp_material WHERE mat_id = #{materialId} AND is_active = TRUE")
    boolean materialExists(@Param("materialId") Long materialId);
    
    /**
     * 获取物料的标准价格
     * @param materialId 物料ID
     * @return 标准价格
     */
    @Select("SELECT srd_price FROM erp_material WHERE mat_id = #{materialId} AND is_active = TRUE")
    BigDecimal getMaterialStandardPrice(@Param("materialId") Long materialId);
    
    /**
     * 获取物料描述信息
     * @param materialId 物料ID
     * @return 物料描述
     */
    @Select("SELECT mat_desc FROM erp_material WHERE mat_id = #{materialId} AND is_active = TRUE")
    String getMaterialDescription(@Param("materialId") Long materialId);
    
    /**
     * 获取物料基本单位
     * @param materialId 物料ID
     * @return 基本单位
     */
    @Select("SELECT base_uom FROM erp_material WHERE mat_id = #{materialId} AND is_active = TRUE")
    String getMaterialBaseUnit(@Param("materialId") Long materialId);
    
    /**
     * 获取物料的货币单位
     * @param materialId 物料ID
     * @return 货币单位
     */
    @Select("SELECT price_unit FROM erp_material WHERE mat_id = #{materialId} AND is_active = TRUE")
    String getMaterialPriceUnit(@Param("materialId") Long materialId);
    
    /**
     * 获取所有有效的定价条件类型
     * @return 定价条件类型列表
     */
    @Select("SELECT cnty, name, description, is_percentage, default_currency, is_active " +
            "FROM erp_pricing_condition_type WHERE is_active = TRUE")
    List<PricingConditionType> getAllActivePricingConditionTypes();
    
    /**
     * 根据条件类型代码获取定价条件类型
     * @param cnty 条件类型代码
     * @return 定价条件类型
     */
    @Select("SELECT cnty, name, description, is_percentage, default_currency, is_active " +
            "FROM erp_pricing_condition_type WHERE cnty = #{cnty} AND is_active = TRUE")
    PricingConditionType getPricingConditionTypeByCnty(@Param("cnty") String cnty);
    
    /**
     * 获取条件类型的默认货币单位
     * @param cnty 条件类型代码
     * @return 默认货币单位
     */
    @Select("SELECT default_city_unit FROM erp_condition_currency_mapping " +
            "WHERE cnty = #{cnty} AND is_default = TRUE LIMIT 1")
    String getDefaultCityUnitByCnty(@Param("cnty") String cnty);
    
    /**
     * 获取应用的验证配置
     * @param applicationType 应用类型
     * @return 税率
     */
    @Select("SELECT tax_rate FROM erp_item_validation_config " +
            "WHERE application_type = #{applicationType} AND is_active = TRUE LIMIT 1")
    BigDecimal getTaxRateByApplicationType(@Param("applicationType") String applicationType);
    
    /**
     * 获取应用的默认货币
     * @param applicationType 应用类型
     * @return 默认货币
     */
    @Select("SELECT default_currency FROM erp_item_validation_config " +
            "WHERE application_type = #{applicationType} AND is_active = TRUE LIMIT 1")
    String getDefaultCurrencyByApplicationType(@Param("applicationType") String applicationType);
    
    /**
     * 获取应用的默认订单概率
     * @param applicationType 应用类型
     * @return 默认订单概率
     */
    @Select("SELECT default_probability FROM erp_item_validation_config " +
            "WHERE application_type = #{applicationType} AND is_active = TRUE LIMIT 1")
    String getDefaultProbabilityByApplicationType(@Param("applicationType") String applicationType);
}