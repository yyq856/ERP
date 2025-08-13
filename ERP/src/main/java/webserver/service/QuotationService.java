package webserver.service;

import webserver.pojo.*;
import webserver.common.Response;

public interface QuotationService {
    Response<QuotationResponseDTO1> createQuotationFromInquiry(CreateQuotationFromInquiryRequest request);
    QuotationResponseDTO1 getQuotationDetails(String quotationId);
    QuotationResponseDTO1 updateQuotation(QuotationResponseDTO1 quotation);
    QuotationSearchResponseDTO searchQuotations(QuotationSearchRequestDTO request);
}
