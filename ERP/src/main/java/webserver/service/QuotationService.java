package webserver.service;

import webserver.pojo.QuotationResponse;
import webserver.pojo.QuotationRequest;

public interface QuotationService {
    QuotationResponse getQuotationDetails(QuotationRequest request);
}
