package webserver.service;

import webserver.pojo.QuotationData;

public interface QuotationService {
    QuotationData createQuotationFromInquiry(String inquiryId);
}
