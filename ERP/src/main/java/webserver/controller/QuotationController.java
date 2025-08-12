package webserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import webserver.common.Response;
import webserver.pojo.CreateQuotationFromInquiryRequest;
import webserver.pojo.QuotationData;
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
            return new Response<>(200, msg, quotationData);
        } catch (Exception e) {
            return new Response<>(500, "Quotation creation failed, please try again later.", null);
        }
    }
}
