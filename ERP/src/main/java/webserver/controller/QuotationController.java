package webserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import webserver.pojo.*;
import webserver.service.QuotationService;
import webserver.common.Response;

import java.util.List;

@Slf4j
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
    public Response<QuotationResponseDTO1> updateQuotation(@RequestBody QuotationUpdateRequest request) {
        try {
            // 转换请求格式：从 {"quotation": {...}} 到 {"quotationData": {...}}
            QuotationResponseDTO1 quotationData = new QuotationResponseDTO1();
            quotationData.setQuotationData(request.getQuotation());

            QuotationResponseDTO1 updated = quotationService.updateQuotation(quotationData);
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

    // ========== 新增：类似inquiry的统一接口 ==========

    /**
     * 初始化报价单
     * @param request 初始化请求
     * @return 响应结果
     */
    @PostMapping("/initialize")
    public QuotationResponse initialize(@RequestBody QuotationInitializeRequest request) {
        log.info("报价单初始化请求");
        return quotationService.initialize(request);
    }

    /**
     * 获取报价单详情 - 支持完整字段
     * @param request 查询请求
     * @return 响应结果
     */
    @PostMapping("/get")
    public QuotationResponse get(@RequestBody QuotationGetRequest request) {
        log.info("报价单查询请求: {}", request.getQuotationId());
        return quotationService.get(request);
    }

    /**
     * 编辑报价单 - 支持完整字段
     * @param request 编辑请求
     * @return 响应结果
     */
    @PostMapping("/edit")
    public QuotationResponse edit(@RequestBody QuotationEditRequest request) {
        log.info("报价单编辑请求");
        return quotationService.edit(request);
    }

    /**
     * 物品批量查询
     * @param items 物品查询列表
     * @return 响应结果
     */
    @PostMapping("/items-tab-query")
    public QuotationResponse itemsTabQuery(@RequestBody List<QuotationItemsTabQueryRequest.ItemQuery> items) {
        log.info("物品批量查询请求，项目数: {}", items != null ? items.size() : 0);
        return quotationService.itemsTabQuery(items);
    }
}
