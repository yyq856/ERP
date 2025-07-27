package webserver.service.impl;

import webserver.pojo.QuotationResponse;
import webserver.mapper.QuotationMapper;
import webserver.pojo.Quotation;
import webserver.pojo.QuotationItem;
import webserver.pojo.QuotationRequest;
import webserver.service.QuotationService;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Service
public class QuotationServiceImpl implements QuotationService {
    private static final Logger log = LoggerFactory.getLogger(QuotationServiceImpl.class);
    
    @Autowired
    private QuotationMapper quotationMapper;

    @Override
    public QuotationResponse getQuotationDetails(QuotationRequest request) {
        try {
            // 1. 验证请求数据
            if (request == null || request.getQuotation_id() == null || request.getQuotation_id().trim().isEmpty()) {
                return createErrorResponse("请求数据不能为空");
            }
            
            Long quotationId = Long.valueOf(request.getQuotation_id());
            
            // 2. 查询报价单头信息
            Quotation quotation = quotationMapper.getQuotationById(quotationId);
            if (quotation == null) {
                return createErrorResponse("报价单不存在");
            }
            
            // 3. 查询报价单项目
            List<QuotationItem> items = quotationMapper.getQuotationItemsByQuotationId(quotationId);
            
            // 4. 构建响应数据
            QuotationResponse response = new QuotationResponse();
            response.setSuccess(true);
            response.setMessage("Quotation data loaded.");
            
            // 创建并设置元数据
            QuotationResponse.Meta meta = new QuotationResponse.Meta();  // 修复：创建meta变量
            meta.setId(quotation.getQuotationId().toString());
            
            // 创建并设置基本数据
            QuotationResponse.BasicInfo basicInfo = new QuotationResponse.BasicInfo();
            basicInfo.setQuotation(quotation.getQuotationId().toString());
            basicInfo.setSoldToParty(quotation.getSoldToParty());
            basicInfo.setShipToParty(quotation.getShipToParty());
            basicInfo.setCustomerReference(quotation.getCustomerReference());
            basicInfo.setNetValue(quotation.getNetValue().toString());
            basicInfo.setNetValueUnit(quotation.getCurrency());
            basicInfo.setCustomerReferenceDate(quotation.getCustomerReferenceDate().toString());
            
            // 创建并设置项目概览
            QuotationResponse.ItemOverview itemOverview = new QuotationResponse.ItemOverview();
            itemOverview.setReqDelivDate(quotation.getReqDeliveryDate().toString());
            
            // 设置项目列表
            List<QuotationResponse.Item> responseItems = items.stream()
                .map(item -> {
                    QuotationResponse.Item responseItem = new QuotationResponse.Item();
                    responseItem.setItem(item.getItemNo().toString());
                    responseItem.setMaterial(item.getMaterial());
                    responseItem.setOrderQuantity(item.getQuantity().toString());
                    responseItem.setOrderQuantityUnit(item.getUnit());
                    responseItem.setDescription(item.getDescription());
                    responseItem.setReqDelivDate(item.getReqDelivDate().toString());
                    responseItem.setNetValue(item.getNetValue().toString());
                    responseItem.setNetValueUnit(item.getNetValueUnit());
                    responseItem.setTaxValue(item.getTaxValue().toString());
                    responseItem.setTaxValueUnit(item.getTaxValueUnit());
                    responseItem.setPricingDate(item.getPricingDate().toString());
                    responseItem.setOrderProbability(item.getOrderProbability());
                    responseItem.setPricingElements(List.of()); // 空列表
                    return responseItem;
                })
                .toList();
            
            itemOverview.setItems(responseItems);
            
            // 创建并设置报价单详情
            QuotationResponse.QuotationDetail quotationDetail = new QuotationResponse.QuotationDetail();
            quotationDetail.setMeta(meta);  // 修复：使用之前创建的meta变量
            quotationDetail.setBasicInfo(basicInfo);
            quotationDetail.setItemOverview(itemOverview);
            
            // 创建并设置响应数据
            QuotationResponse.QuotationData data = new QuotationResponse.QuotationData();
            data.setQuotationData(quotationDetail);
            
            response.setData(data);
            return response;
            
        } catch (Exception e) {
            log.error("获取报价单详情失败: ", e);
            return createErrorResponse("Operation failed: " + e.getMessage());
        }
    }
    
    private QuotationResponse createErrorResponse(String message) {
        QuotationResponse response = new QuotationResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
