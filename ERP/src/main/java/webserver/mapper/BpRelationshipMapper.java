package webserver.mapper;

import org.apache.ibatis.annotations.*;
import webserver.pojo.BpRelationship;

@Mapper
public interface BpRelationshipMapper {
    
    /**
     * 根据关系ID查询业务伙伴关系（包含扩展字段）
     * @param relationId 关系ID
     * @return 业务伙伴关系信息
     */
    @Select("SELECT relation_id as relationId, rel_category as relCategory, bp1, bp2, " +
            "management, department, `function`, valid_from as validFrom, valid_to as validTo, " +
            "customer_code as customerCode, customer_name as customerName, contact_person as contactPerson, " +
            "test_field as testField, description, extended_data as extendedData, " +
            "created_at as createdAt, updated_at as updatedAt " +
            "FROM erp_relation WHERE relation_id = #{relationId}")
    BpRelationship findByRelationId(@Param("relationId") Long relationId);
    
    /**
     * 插入新的业务伙伴关系（包含扩展字段）
     * @param relationship 业务伙伴关系信息
     * @return 影响的行数
     */
    @Insert("INSERT INTO erp_relation (rel_category, bp1, bp2, management, department, `function`, valid_from, valid_to, " +
            "customer_code, customer_name, contact_person, test_field, description, extended_data) " +
            "VALUES (#{relCategory}, #{bp1}, #{bp2}, 1, '01', '01', #{validFrom}, #{validTo}, " +
            "#{customerCode}, #{customerName}, #{contactPerson}, #{testField}, #{description}, #{extendedData})")
    @Options(useGeneratedKeys = true, keyProperty = "relationId")
    int insertRelationship(BpRelationship relationship);
    
    /**
     * 更新业务伙伴关系（包含扩展字段）
     * @param relationship 业务伙伴关系信息
     * @return 影响的行数
     */
    @Update("UPDATE erp_relation SET rel_category = #{relCategory}, bp1 = #{bp1}, bp2 = #{bp2}, " +
            "management = 1, department = '01', `function` = '01', " +
            "valid_from = #{validFrom}, valid_to = #{validTo}, " +
            "customer_code = #{customerCode}, customer_name = #{customerName}, contact_person = #{contactPerson}, " +
            "test_field = #{testField}, description = #{description}, extended_data = #{extendedData} " +
            "WHERE relation_id = #{relationId}")
    int updateRelationship(BpRelationship relationship);
    
    /**
     * 检查关系是否存在
     * @param relationId 关系ID
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM erp_relation WHERE relation_id = #{relationId}")
    int countByRelationId(@Param("relationId") Long relationId);
}
