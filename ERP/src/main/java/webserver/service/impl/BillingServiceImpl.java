package webserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webserver.mapper.BillingMapper;
import webserver.pojo.BillingSearchRequest;
import webserver.service.BillingService;
import webserver.pojo.BillingInitializeRequest;
import webserver.pojo.BillingGetRequest;
import webserver.pojo.BillingEditRequest;
import webserver.pojo.ItemValidationRequest.Item;
import webserver.pojo.ItemValidationRequest.PricingElement;

import java.util.*;

@Service
public class BillingServiceImpl implements BillingService {
    
    @Autowired
    private BillingMapper billingMapper;
    
    @Override
    public Map<String, Object> initializeBilling(BillingInitializeRequest request) {
        String billingDate = request.getBillingDueList().getBillingDate();
        String soldToParty = request.getBillingDueList().getSoldToParty();
        
        // 获取客户信息
        Map<String, Object> customer = billingMapper.getCustomerBySoldToParty(soldToParty);
        
        // 构建响应数据结构
        Map<String, Object> result = new HashMap<>();
        
        // meta部分
        Map<String, Object> meta = new HashMap<>();
        meta.put("id", "");
        result.put("meta", meta);
        
        // basicInfo部分
        Map<String, Object> basicInfo = new HashMap<>();
        basicInfo.put("type", "Invoice");
        basicInfo.put("id", "");
        basicInfo.put("netValue", "0.00");
        basicInfo.put("netValueUnit", "USD");
        basicInfo.put("payer", customer != null ? customer.get("name") : soldToParty);
        basicInfo.put("billingDate", billingDate);
        result.put("basicInfo", basicInfo);
        
        // itemOverview部分
        Map<String, Object> itemOverview = new HashMap<>();
        List<Map<String, Object>> items = billingMapper.getBillingItems(billingDate, soldToParty);
        itemOverview.put("items", items);
        result.put("itemOverview", itemOverview);
        
        Map<String, Object> content = new HashMap<>();
        content.put("content", result);
        
        return content;
    }
    
    @Override
    public Map<String, Object> getBilling(BillingGetRequest request) {
        String billingDocumentId = request.getBillingDocumentId();
        
        // 获取开票凭证基本信息
        Map<String, Object> billingHeader = billingMapper.getBillingHeader(billingDocumentId);
        if (billingHeader == null) {
            return null; // 未找到开票凭证
        }
        
        // 获取开票凭证项目信息
        List<Map<String, Object>> billingItems = billingMapper.getBillingItemsById(billingDocumentId);
        
        // 构建响应数据结构
        Map<String, Object> result = new HashMap<>();
        
        // meta部分
        Map<String, Object> meta = new HashMap<>();
        meta.put("id", billingDocumentId);
        result.put("meta", meta);
        
        // basicInfo部分
        Map<String, Object> basicInfo = new HashMap<>();
        basicInfo.put("type", "Invoice");
        basicInfo.put("id", billingDocumentId);
        basicInfo.put("netValue", billingHeader.get("netValue"));
        basicInfo.put("netValueUnit", billingHeader.get("netValueUnit"));
        basicInfo.put("payer", billingHeader.get("payer"));
        basicInfo.put("billingDate", billingHeader.get("billingDate"));
        result.put("basicInfo", basicInfo);
        
        // itemOverview部分
        Map<String, Object> itemOverview = new HashMap<>();
        itemOverview.put("items", billingItems);
        result.put("itemOverview", itemOverview);
        
        Map<String, Object> content = new HashMap<>();
        content.put("content", result);
        
        return content;
    }
    
    @Override
    public Map<String, Object> editBilling(BillingEditRequest request) {
        String billingId = request.getMeta().getId();
        String message;
        
        // 验证交货单是否存在
        if (request.getItemOverview() != null && 
            request.getItemOverview().getItems() != null && 
            !request.getItemOverview().getItems().isEmpty()) {
            
            String dlvId = request.getItemOverview().getItems().get(0).getDlvId();
            int deliveryExists = billingMapper.checkDeliveryExists(dlvId);
            
            if (deliveryExists == 0) {
                throw new RuntimeException("Delivery ID " + dlvId + " does not exist in the system");
            }
        }
        
        if (billingId == null || billingId.isEmpty()) {
            // 创建新的开票凭证
            billingId = billingMapper.createBilling(request);
            message = "Billing document " + billingId + " created successfully";
        } else {
            // 更新现有开票凭证
            billingMapper.updateBilling(request);
            message = "Billing document " + billingId + " updated successfully";
        }
        
        // 处理项目信息
        if (request.getItemOverview() != null && 
            request.getItemOverview().getItems() != null) {
            
            // 删除现有项目（如果是更新）
            if (billingId != null && !billingId.isEmpty()) {
                billingMapper.deleteBillingItems(billingId);
            }
            
            // 插入新项目
            for (BillingEditRequest.Item item : request.getItemOverview().getItems()) {
                billingMapper.insertBillingItem(billingId, item);
            }
        }
        
        // 获取更新后的开票凭证信息
        BillingGetRequest getRequest = new BillingGetRequest();
        getRequest.setBillingDocumentId(billingId);
        Map<String, Object> result = getBilling(getRequest);
        
        // 添加消息
        if (result != null && result.containsKey("content")) {
            Map<String, Object> content = (Map<String, Object>) result.get("content");
            content.put("message", message);
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> searchBilling(BillingSearchRequest request) {
        List<Map<String, Object>> billingList = billingMapper.searchBilling(request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", billingList);
        
        return result;
    }
    
    @Override
    public Map<String, Object> validateBillingItems(List<Item> items) {
        Map<String, Object> response = new HashMap<>();
        
        // 构建响应数据
        Map<String, Object> data = new HashMap<>();
        
        // result部分
        Map<String, Object> result = new HashMap<>();
        result.put("allDataLegal", 1); // 默认所有数据合法
        result.put("badRecordIndices", new ArrayList<>()); // 默认没有不合法数据
        data.put("result", result);
        
        // generalData部分
        Map<String, Object> generalData = new HashMap<>();
        double totalNetValue = 0;
        String currency = "USD"; // 默认货币
        
        // breakdowns部分
        List<Map<String, Object>> breakdowns = new ArrayList<>();
        
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            
            Map<String, Object> breakdown = new HashMap<>();
            breakdown.put("item", item.getItem());
            breakdown.put("material", item.getMaterial());
            breakdown.put("orderQuantity", item.getOrderQuantity());
            breakdown.put("orderQuantityUnit", item.getOrderQuantityUnit());
            breakdown.put("description", item.getDescription());
            breakdown.put("reqDelivDate", item.getReqDelivDate());
            breakdown.put("pricingDate", item.getPricingDate());
            breakdown.put("orderProbability", item.getOrderProbability());
            
            // 计算净值和税值
            double netValue = 0;
            double taxValue = 0;
            
            try {
                double quantity = Double.parseDouble(item.getOrderQuantity());
                // 从定价元素中获取价格信息
                if (item.getPricingElements() != null && !item.getPricingElements().isEmpty()) {
                    for (PricingElement element : item.getPricingElements()) {
                        if ("Base Price".equals(element.getName())) {
                            netValue = quantity * Double.parseDouble(element.getConditionValue());
                            currency = element.getCurr();
                            break;
                        }
                    }
                }
                
                // 如果没有定价元素或找不到基准价格，则使用默认逻辑
                if (netValue == 0) {
                    // 假设单价为100
                    netValue = quantity * 100;
                }
                
                // 计算税值（假设税率10%）
                taxValue = netValue * 0.1;
                
                // 累加到总净值
                totalNetValue += netValue;
            } catch (NumberFormatException e) {
                // 如果解析失败，标记数据不合法
                result.put("allDataLegal", 0);
                List<Integer> badIndices = (List<Integer>) result.get("badRecordIndices");
                badIndices.add(i);
            } catch (Exception e) {
                // 其他异常也标记为不合法
                result.put("allDataLegal", 0);
                List<Integer> badIndices = (List<Integer>) result.get("badRecordIndices");
                badIndices.add(i);
            }
            
            breakdown.put("netValue", netValue);
            breakdown.put("netValueUnit", currency != null ? currency : item.getNetValueUnit());
            breakdown.put("taxValue", taxValue);
            breakdown.put("taxValueUnit", currency != null ? currency : item.getTaxValueUnit());
            
            // 复制定价元素
            if (item.getPricingElements() != null) {
                breakdown.put("pricingElements", item.getPricingElements());
            } else {
                breakdown.put("pricingElements", new ArrayList<>());
            }
            
            breakdowns.add(breakdown);
        }
        
        generalData.put("netValue", String.format("%.2f", totalNetValue));
        generalData.put("netValueUnit", currency);
        generalData.put("expectOralVal", String.format("%.2f", totalNetValue * 1.1)); // 预期口头值比净值高10%
        generalData.put("expectOralValUnit", currency);
        
        data.put("generalData", generalData);
        data.put("breakdowns", breakdowns);
        
        response.put("success", true);
        response.put("message", "批量验证成功");
        response.put("data", data);
        
        return response;
    }
}
