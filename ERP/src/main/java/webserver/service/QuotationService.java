package webserver.service;

import webserver.pojo.QuotationData;
import webserver.pojo.QuotationDetailsResponse;

public interface QuotationService {
    QuotationData createQuotationFromInquiry(String inquiryId);
    QuotationDetailsResponse getQuotationDetails(String salesQuotationId);
    QuotationDetailsResponse updateQuotation(QuotationDetailsResponse quotation);
}
