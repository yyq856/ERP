package webserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import webserver.pojo.*;
import webserver.service.QuotationService;
import webserver.common.Response;

@RestController
@RequestMapping("/api/quotation")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;

    @PostMapping("/create-quotation-from-inquiry")
    public Response<QuotationResponseDTO> createQuotationFromInquiry(
            @RequestBody CreateQuotationFromInquiryRequest request) {
        return quotationService.createQuotationFromInquiry(request);
    }

    @PostMapping("/details")
    public Response<QuotationDetailsResponseDTO> getQuotationDetails(@RequestBody QuotationDetailsRequestDTO request) {
        try {
            QuotationDetailsResponseDTO data = quotationService.getQuotationDetails(request.getSalesQuotationId());
            return Response.success(data);
        } catch (Exception e) {
            return Response.error("Failed to load quotation details: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    public Response<QuotationResponseDTO> updateQuotation(@RequestBody QuotationResponseDTO quotation) {
        try {
            QuotationResponseDTO updated = quotationService.updateQuotation(quotation);
            return Response.success(updated);
        } catch (Exception e) {
            return Response.error("Failed to update quotation: " + e.getMessage());
        }
    }

    @PostMapping("/search")
    public Response<QuotationSearchResponseDTO> searchQuotations(@RequestBody QuotationSearchRequestDTO request) {
        try {
            QuotationSearchResponseDTO data = quotationService.searchQuotations(request);
            return Response.success(data);
        } catch (Exception e) {
            return Response.error("Quotation not found: " + e.getMessage());
        }
    }
}
