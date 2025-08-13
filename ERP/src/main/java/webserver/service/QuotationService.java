package webserver.service;

import webserver.pojo.*;
import webserver.common.Response;

public interface QuotationService {
    Response<QuotationResponseDTO> createQuotationFromInquiry(CreateQuotationFromInquiryRequest request);
    QuotationDetailsResponseDTO getQuotationDetails(String quotationId);
    QuotationResponseDTO updateQuotation(QuotationResponseDTO quotation);
    QuotationSearchResponseDTO searchQuotations(QuotationSearchRequestDTO request);
}
