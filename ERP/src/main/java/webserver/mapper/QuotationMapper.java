package webserver.mapper;

import webserver.pojo.Quotation;
import webserver.pojo.QuotationItem;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QuotationMapper {
    // 获取报价单头信息
    Quotation getQuotationById(@Param("quotationId") Long quotationId);
    
    // 获取报价单项目列表
    List<QuotationItem> getQuotationItemsByQuotationId(@Param("quotationId") Long quotationId);
    
    // 获取报价单的完整信息（用于自动填充销售订单）
    Map<String, Object> getQuotationDetails(@Param("quotationId") Long quotationId);
    
    // 获取报价单项目列表的完整信息
    List<Map<String, Object>> getQuotationItemsDetails(@Param("quotationId") Long quotationId);
}
