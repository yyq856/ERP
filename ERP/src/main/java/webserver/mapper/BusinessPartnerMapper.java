package webserver.mapper;

import org.apache.ibatis.annotations.*;
import webserver.pojo.BusinessPartner;
import webserver.pojo.BpMaintainSearchResponse;
import java.util.List;

@Mapper
public interface BusinessPartnerMapper {

    /**
     * 根据客户ID查询业务伙伴
     * @param customerId 客户ID
     * @return 业务伙伴信息
     */
    @Select("SELECT customer_id as customerId, title, name, language, street, city, region, " +
            "postal_code as postalCode, country, company_code as companyCode, " +
            "reconciliation_account as reconciliationAccount, sort_key as sortKey, " +
            "sales_org as salesOrg, channel, division, currency, sales_district as salesDistrict, " +
            "price_group as priceGroup, customer_group as customerGroup, " +
            "delivery_priority as deliveryPriority, shipping_condition as shippingCondition, " +
            "delivering_plant as deliveringPlant, max_part_deliv as maxPartDeliv, " +
            "incoterms, incoterms_location as incotermsLocation, payment_terms as paymentTerms, " +
            "acct_assignment as acctAssignment, output_tax as outputTax " +
            "FROM erp_customer WHERE customer_id = #{customerId}")
    BusinessPartner findByCustomerId(@Param("customerId") String customerId);

    /**
     * 插入新的业务伙伴
     * @param bp 业务伙伴信息
     * @return 影响的行数
     */
    @Insert("INSERT INTO erp_customer (title, name, language, street, city, region, postal_code, " +
            "country, company_code, reconciliation_account, sort_key, sales_org, channel, " +
            "division, currency, sales_district, price_group, customer_group, delivery_priority, " +
            "shipping_condition, delivering_plant, max_part_deliv, incoterms, incoterms_location, " +
            "payment_terms, acct_assignment, output_tax) " +
            "VALUES (#{title}, #{firstName}, #{language}, #{street}, #{city}, #{region}, #{postalCode}, " +
            "#{country}, #{companyCode}, #{reconciliationAccount}, #{sortKey}, #{salesOrg}, #{channel}, " +
            "#{division}, #{currency}, #{salesDistrict}, #{priceGroup}, #{customerGroup}, #{deliveryPriority}, " +
            "#{shippingCondition}, #{deliveringPlant}, #{maxPartDeliv}, #{incoterms}, #{incotermsLocation}, " +
            "#{paymentTerms}, #{acctAssignment}, #{outputTax})")
    @Options(useGeneratedKeys = true, keyProperty = "customerId")
    int insertBusinessPartner(BusinessPartner bp);

    /**
     * 检查客户ID是否存在
     * @param customerId 客户ID
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM erp_customer WHERE customer_id = #{customerId}")
    int countByCustomerId(@Param("customerId") String customerId);

    /**
     * BP维护页面 - 搜索业务伙伴（支持模糊查询）
     * @param customerId 客户ID（支持模糊查询）
     * @return 业务伙伴列表
     */
    @Select("SELECT customer_id as customerId, title, name, first_name as firstName, last_name as lastName, " +
            "city, country, bp_type as type " +
            "FROM erp_customer " +
            "WHERE customer_id LIKE CONCAT('%', #{customerId}, '%') " +
            "ORDER BY customer_id")
    List<BpMaintainSearchResponse.BpSearchItem> searchBusinessPartnersForMaintain(@Param("customerId") String customerId);

    /**
     * BP维护页面 - 根据ID获取业务伙伴详情
     * @param customerId 客户ID
     * @return 业务伙伴详情
     */
    @Select("SELECT customer_id as customerId, title, name, first_name as firstName, last_name as lastName, " +
            "bp_type as bpType, search_term as searchTerm, language, street, city, region, postal_code as postalCode, country, " +
            "company_code as companyCode, reconciliation_account as reconciliationAccount, sort_key as sortKey, " +
            "sales_org as salesOrg, channel, division, currency, sales_district as salesDistrict, " +
            "price_group as priceGroup, customer_group as customerGroup, delivery_priority as deliveryPriority, " +
            "shipping_condition as shippingCondition, delivering_plant as deliveringPlant, max_part_deliv as maxPartDeliv, " +
            "incoterms, incoterms_location as incotermsLocation, payment_terms as paymentTerms, " +
            "acct_assignment as acctAssignment, output_tax as outputTax " +
            "FROM erp_customer WHERE customer_id = #{customerId}")
    BusinessPartner findBusinessPartnerDetailById(@Param("customerId") String customerId);

    /**
     * BP维护页面 - 更新业务伙伴信息
     * @param bp 业务伙伴信息
     * @return 影响的行数
     */
    @Update("UPDATE erp_customer SET title = #{title}, name = #{firstName}, first_name = #{firstName}, " +
            "last_name = #{lastName}, bp_type = #{bpType}, search_term = #{searchTerm}, " +
            "street = #{street}, city = #{city}, postal_code = #{postalCode}, country = #{country} " +
            "WHERE customer_id = #{customerId}")
    int updateBusinessPartner(BusinessPartner bp);

    /**
     * BP维护页面 - 插入新的业务伙伴（简化版）
     * 注意：此方法的实现在BusinessPartnerMapper.xml中
     * @param bp 业务伙伴信息
     * @return 影响的行数
     */
    int insertBusinessPartnerForMaintain(BusinessPartner bp);
}
