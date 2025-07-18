package webserver.mapper;

import org.apache.ibatis.annotations.*;
import webserver.pojo.Inquiry;
import webserver.pojo.InquiryItem;

import java.util.List;

@Mapper
public interface InquiryMapper {
    
    /**
     * 根据询价单ID查询询价单
     * @param inquiryId 询价单ID
     * @return 询价单信息
     */
    @Select("SELECT inquiry_id as inquiryId, cust_id as custId, inquiry_type as inquiryType, " +
            "sls_org as slsOrg, sales_district as salesDistrict, division, sold_tp as soldTp, " +
            "ship_tp as shipTp, cust_ref as custRef, customer_reference_date as customerReferenceDate, " +
            "valid_from_date as validFromDate, valid_to_date as validToDate, probability, " +
            "net_value as netValue, status " +
            "FROM erp_inquiry WHERE inquiry_id = #{inquiryId}")
    Inquiry findByInquiryId(@Param("inquiryId") Long inquiryId);
    
    /**
     * 插入新的询价单
     * @param inquiry 询价单信息
     * @return 影响的行数
     */
    @Insert("INSERT INTO erp_inquiry (cust_id, inquiry_type, sls_org, sales_district, division, " +
            "sold_tp, ship_tp, cust_ref, customer_reference_date, valid_from_date, valid_to_date, " +
            "probability, net_value, status) " +
            "VALUES (#{custId}, #{inquiryType}, #{slsOrg}, #{salesDistrict}, #{division}, " +
            "#{soldTp}, #{shipTp}, #{custRef}, #{customerReferenceDate}, #{validFromDate}, " +
            "#{validToDate}, #{probability}, #{netValue}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "inquiryId")
    int insertInquiry(Inquiry inquiry);
    
    /**
     * 更新询价单
     * @param inquiry 询价单信息
     * @return 影响的行数
     */
    @Update("UPDATE erp_inquiry SET cust_id = #{custId}, inquiry_type = #{inquiryType}, " +
            "sls_org = #{slsOrg}, sales_district = #{salesDistrict}, division = #{division}, " +
            "sold_tp = #{soldTp}, ship_tp = #{shipTp}, cust_ref = #{custRef}, " +
            "customer_reference_date = #{customerReferenceDate}, valid_from_date = #{validFromDate}, " +
            "valid_to_date = #{validToDate}, probability = #{probability}, net_value = #{netValue}, " +
            "status = #{status} WHERE inquiry_id = #{inquiryId}")
    int updateInquiry(Inquiry inquiry);
    
    /**
     * 根据询价单ID查询询价单项目
     * @param inquiryId 询价单ID
     * @return 询价单项目列表
     */
    @Select("SELECT inquiry_id as inquiryId, item_no as itemNo, mat_id as matId, quantity, " +
            "net_price as netPrice, item_value as itemValue, plant_id as plantId, su " +
            "FROM erp_inquiry_item WHERE inquiry_id = #{inquiryId} ORDER BY item_no")
    List<InquiryItem> findItemsByInquiryId(@Param("inquiryId") Long inquiryId);
    
    /**
     * 插入询价单项目
     * @param item 询价单项目
     * @return 影响的行数
     */
    @Insert("INSERT INTO erp_inquiry_item (inquiry_id, item_no, mat_id, quantity, net_price, " +
            "item_value, plant_id, su) VALUES (#{inquiryId}, #{itemNo}, #{matId}, #{quantity}, " +
            "#{netPrice}, #{itemValue}, #{plantId}, #{su})")
    int insertInquiryItem(InquiryItem item);
    
    /**
     * 删除询价单的所有项目
     * @param inquiryId 询价单ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM erp_inquiry_item WHERE inquiry_id = #{inquiryId}")
    int deleteInquiryItems(@Param("inquiryId") Long inquiryId);
    
    /**
     * 检查询价单是否存在
     * @param inquiryId 询价单ID
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM erp_inquiry WHERE inquiry_id = #{inquiryId}")
    int countByInquiryId(@Param("inquiryId") Long inquiryId);
}
