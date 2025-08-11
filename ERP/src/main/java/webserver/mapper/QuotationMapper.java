package webserver.mapper;

import org.apache.ibatis.annotations.Param;
import webserver.pojo.QuotationBasicInfo;
import webserver.pojo.QuotationDetailsResponse;
import webserver.pojo.QuotationItem;
import webserver.pojo.QuotationItemOverview;

import java.util.List;

public interface QuotationMapper {

    String insertQuotationFromInquiry(@Param("inquiryId") String inquiryId);

    QuotationBasicInfo getQuotationBasicInfo(@Param("quotationId") String quotationId);

    QuotationItemOverview getQuotationItemOverview(@Param("quotationId") String quotationId);

    List<QuotationItem> getQuotationItems(@Param("quotationId") String quotationId);

    QuotationDetailsResponse.BasicInfo getQuotationBasicInfo1(@Param("quotationId") String quotationId);

    List<QuotationDetailsResponse.Item> getQuotationItems1(@Param("quotationId") String quotationId);

    void updateQuotationBasicInfo(QuotationDetailsResponse.BasicInfo basicInfo);

    void deleteQuotationItems(@Param("quotationId") String quotationId);

    void insertQuotationItems(@Param("quotationId") String quotationId,
                              @Param("items") List<QuotationDetailsResponse.Item> items);
}
