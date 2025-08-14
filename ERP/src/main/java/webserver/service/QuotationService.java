package webserver.service;

import webserver.pojo.*;
import webserver.common.Response;
import java.util.List;

public interface QuotationService {
    Response<QuotationResponseDTO1> createQuotationFromInquiry(CreateQuotationFromInquiryRequest request);
    QuotationResponseDTO1 getQuotationDetails(String quotationId);
    QuotationResponseDTO1 updateQuotation(QuotationResponseDTO1 quotation);
    QuotationSearchResponseDTO searchQuotations(QuotationSearchRequestDTO request);

    // ========== 新增：支持完整ItemValidation字段的方法 ==========

    /**
     * 初始化报价单
     * @param request 初始化请求
     * @return 响应结果
     */
    QuotationResponse initialize(QuotationInitializeRequest request);

    /**
     * 获取报价单详情 - 支持完整字段
     * @param request 查询请求
     * @return 响应结果
     */
    QuotationResponse get(QuotationGetRequest request);

    /**
     * 编辑报价单 - 支持完整字段
     * @param request 编辑请求
     * @return 响应结果
     */
    QuotationResponse edit(QuotationEditRequest request);

    /**
     * 物品批量查询
     * @param items 物品查询列表
     * @return 响应结果
     */
    QuotationResponse itemsTabQuery(List<QuotationItemsTabQueryRequest.ItemQuery> items);
}
