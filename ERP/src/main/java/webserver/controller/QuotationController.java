package webserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import webserver.pojo.*;
import webserver.service.QuotationService;
import webserver.common.Response;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/quotation")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;

    @PostMapping("/create-quotation-from-inquiry")
    public Response<QuotationResponseDTO1> createQuotationFromInquiry(
            @RequestBody CreateQuotationFromInquiryRequest request) {
        return quotationService.createQuotationFromInquiry(request);
    }

    @PostMapping("/details")
    public Response<QuotationResponseDTO1> getQuotationDetails(@RequestBody QuotationDetailsRequestDTO request) {
        try {
            QuotationResponseDTO1 data = quotationService.getQuotationDetails(request.getSalesQuotationId());
            return Response.success(data);
        } catch (Exception e) {
            return Response.error("Failed to load quotation details: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    public Response<QuotationResponseDTO1> updateQuotation(@RequestBody QuotationResponseDTO1 quotation) {
        try {
            QuotationResponseDTO1 updated = quotationService.updateQuotation(quotation);
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
