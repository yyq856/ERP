package webserver.controller;

import webserver.pojo.QuotationRequest;
import webserver.pojo.QuotationResponse;
import webserver.service.QuotationService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
@RequestMapping("/api/quotation")
public class QuotationController {
    @Autowired
    private QuotationService quotationService;

    @PostMapping("/details")
    @ResponseBody
    public QuotationResponse getQuotationDetails(@RequestBody QuotationRequest request) {
        return quotationService.getQuotationDetails(request);
    }
}
