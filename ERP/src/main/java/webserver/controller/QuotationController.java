package webserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import webserver.common.Response;
import webserver.pojo.*;
import webserver.service.QuotationService;

@RestController
@RequestMapping("/api/quotation")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;

    @PostMapping("/create-quotation-from-inquiry")
    public Response<QuotationData> createQuotationFromInquiry(@RequestBody CreateQuotationFromInquiryRequest request) {
        try {
            QuotationData quotationData = quotationService.createQuotationFromInquiry(request.getInquiryId());
            String msg = String.format("根据inquiry{%s}成功创建报价单{%s}", request.getInquiryId(), quotationData.getBasicInfo().getQuotation());
            return new Response<>(200, msg, true, quotationData);
        } catch (Exception e) {
            return new Response<>(500, "Quotation creation failed, please try again later.", false, null);
        }
    }

    @PostMapping("/details")
    public Response<QuotationDetailsResponse> getQuotationDetails(@RequestBody QuotationDetailsRequest request) {
        try {
            QuotationDetailsResponse response = quotationService.getQuotationDetails(request.getSalesQuotationId());
            return new Response<>(200, "初始化quotation{" + request.getSalesQuotationId() + "}成功", true, response);
        } catch (Exception e) {
            return new Response<>(500, "Failed to load quotation details.", false, null);
        }
    }

    @PostMapping("/update")
    public Response<QuotationDetailsResponse> updateQuotation(@RequestBody QuotationUpdateRequest request) {
        try {
            QuotationDetailsResponse updatedQuotation = quotationService.updateQuotation(request.getQuotation());
            return new Response<>(200, "Sales Order saved successfully!", true, updatedQuotation);
        } catch (Exception e) {
            return new Response<>(500, "Update failed.", false, null);
        }
    }
}
