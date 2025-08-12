package webserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        
        // 为每个项目添加定价元素
        for (Map<String, Object> item : items) {
            // 从项目中获取必要的信息来查询定价元素
            String dlvId = (String) item.get("dlvId");
            if (dlvId != null) {
                // 获取交货单对应的销售订单ID和项目号
                Map<String, Object> deliveryInfo = billingMapper.getDeliveryInfo(dlvId);
                if (deliveryInfo != null) {
                    Long soId = (Long) deliveryInfo.get("soId");
                    Integer itemNo = (Integer) deliveryInfo.get("itemNo");
                    
                    if (soId != null && itemNo != null) {
                        // 获取定价元素
                        List<Map<String, Object>> pricingElements = billingMapper.getPricingElements(soId, itemNo);
                        item.put("pricingElements", pricingElements);
                    }
                }
            }
        }
        
        itemOverview.put("items", items);
        result.put("itemOverview", itemOverview);
        
        return result;
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
        
        // 为每个项目添加定价元素
        for (Map<String, Object> item : billingItems) {
            // 从项目中获取交货单ID
            Object dlvIdObj = item.get("dlvId");
            if (dlvIdObj != null) {
                String dlvId = dlvIdObj.toString();
                // 获取交货单对应的销售订单ID和项目号
                Map<String, Object> deliveryInfo = billingMapper.getDeliveryInfo(dlvId);
                if (deliveryInfo != null) {
                    Long soId = (Long) deliveryInfo.get("soId");
                    Integer itemNo = (Integer) deliveryInfo.get("itemNo");
                    
                    if (soId != null && itemNo != null) {
                        // 获取定价元素
                        List<Map<String, Object>> pricingElements = billingMapper.getPricingElements(soId, itemNo);
                        item.put("pricingElements", pricingElements);
                    }
                }
            }
        }
        
        // 构建响应数据结构
        Map<String, Object> result = new HashMap<>();
        
        // meta部分
        Map<String, Object> meta = new HashMap<>();
        meta.put("id", billingDocumentId);
        result.put("meta", meta);
        
        // basicInfo部分
        Map<String, Object> basicInfo = new HashMap<>();
        basicInfo.put("type", billingHeader.get("type"));
        basicInfo.put("id", billingHeader.get("id"));
        basicInfo.put("netValue", billingHeader.get("netValue"));
        basicInfo.put("netValueUnit", billingHeader.get("netValueUnit"));
        basicInfo.put("payer", billingHeader.get("payer"));
        basicInfo.put("billingDate", billingHeader.get("billingDate"));
        result.put("basicInfo", basicInfo);
        
        // itemOverview部分
        Map<String, Object> itemOverview = new HashMap<>();
        itemOverview.put("items", billingItems);
        result.put("itemOverview", itemOverview);
        
        return result;
    }
    
    @Override
    @Transactional
    public Map<String, Object> editBilling(BillingEditRequest request) {
        BillingEditRequest.Meta meta = request.getMeta();
        BillingEditRequest.BasicInfo basicInfo = request.getBasicInfo();
        BillingEditRequest.ItemOverview itemOverview = request.getItemOverview();
        
        String billingId = meta.getId();
        boolean isUpdate = billingId != null && !billingId.isEmpty();
        
        try {
            if (isUpdate) {
                // 更新现有开票凭证
                billingMapper.updateBilling(request);
                // 删除现有项目
                billingMapper.deleteBillingItems(billingId);
            } else {
                // 创建新开票凭证
                billingMapper.createBilling(request);
                // 获取生成的ID
                billingId = String.valueOf(request.getBasicInfo().getId());
            }
            
            // 插入项目
            List<BillingEditRequest.Item> items = itemOverview.getItems();
            if (items != null && !items.isEmpty()) {
                for (int i = 0; i < items.size(); i++) {
                    BillingEditRequest.Item item = items.get(i);
                    // 添加项目号
                    item.setItemNo(String.valueOf(i + 1));
                    billingMapper.insertBillingItem(billingId, item);
                }
            }
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> responseMeta = new HashMap<>();
            responseMeta.put("id", billingId);
            result.put("meta", responseMeta);
            
            result.put("basicInfo", basicInfo);
            result.put("itemOverview", itemOverview);
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("保存开票凭证失败: " + e.getMessage(), e);
        }
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
