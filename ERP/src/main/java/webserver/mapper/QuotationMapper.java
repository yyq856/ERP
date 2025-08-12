package webserver.mapper;

import org.apache.ibatis.annotations.Param;
import webserver.pojo.QuotationBasicInfo;
import webserver.pojo.QuotationItem;
import webserver.pojo.QuotationItemOverview;

import java.util.List;

public interface QuotationMapper {

    String insertQuotationFromInquiry(@Param("inquiryId") String inquiryId);

    QuotationBasicInfo getQuotationBasicInfo(@Param("quotationId") String quotationId);

    QuotationItemOverview getQuotationItemOverview(@Param("quotationId") String quotationId);

    List<QuotationItem> getQuotationItems(@Param("quotationId") String quotationId);
}
