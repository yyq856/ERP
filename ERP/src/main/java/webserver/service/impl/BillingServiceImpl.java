package webserver.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger log = LoggerFactory.getLogger(BillingServiceImpl.class);
    
    @Autowired
    private BillingMapper billingMapper;
    
    @Override
    public Map<String, Object> initializeBilling(BillingInitializeRequest request) {
        String billingDate = request.getBillingDueList().getBillingDate();
        String soldToParty = request.getBillingDueList().getSoldToParty();
        String deliveryId = request.getBillingDueList().getDeliveryId();
        
        // 验证逻辑
        if (deliveryId != null && !deliveryId.isEmpty()) {
            // 检查delivery是否已完成
            boolean isCompleted = billingMapper.isDeliveryCompleted(deliveryId);
            if (!isCompleted) {
                throw new RuntimeException("指定的交货单尚未完成");
            }
            
            // 获取交货单信息
            Map<String, Object> deliveryInfo = billingMapper.getDeliveryById(deliveryId);
            if (deliveryInfo == null) {
                throw new RuntimeException("未找到指定的交货单");
            }
            
            // 如果指定了soldToParty，需要验证一致性
            if (soldToParty != null && !soldToParty.isEmpty()) {
                Object customerIdObj = deliveryInfo.get("customerId");
                String customerId = customerIdObj != null ? customerIdObj.toString() : null;
                if (customerId == null || !customerId.equals(soldToParty)) {
                    throw new RuntimeException("交货单的客户与指定的售达方不一致");
                }
            } else {
                // 如果没有指定soldToParty，使用交货单的客户
                Object customerIdObj = deliveryInfo.get("customerId");
                soldToParty = customerIdObj != null ? customerIdObj.toString() : null;
            }
            
            // 如果没有指定billingDate，使用默认值（今天）
            if (billingDate == null || billingDate.isEmpty()) {
                billingDate = java.time.LocalDate.now().toString();
            }
        }
        
        // 获取客户信息
        Map<String, Object> customer = null;
        if (soldToParty != null && !soldToParty.isEmpty()) {
            customer = billingMapper.getCustomerBySoldToParty(soldToParty);
        }
        
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
        basicInfo.put("deliveryId", deliveryId != null ? deliveryId : "");
        basicInfo.put("netValue", "0.00");
        basicInfo.put("netValueUnit", "USD");
        basicInfo.put("payer", customer != null ? customer.get("name") : (soldToParty != null ? soldToParty : ""));
        basicInfo.put("billingDate", billingDate != null ? billingDate : java.time.LocalDate.now().toString());
        result.put("basicInfo", basicInfo);
        
        // itemOverview部分
        Map<String, Object> itemOverview = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();
        
        // 如果指定了deliveryId，从交货单获取项目信息
        if (deliveryId != null && !deliveryId.isEmpty()) {
            items = billingMapper.getBillingItemsByDeliveryId(deliveryId);
            
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
        // 添加deliveryId字段（如果数据库中有对应字段）
        basicInfo.put("deliveryId", billingHeader.get("deliveryId") != null ? billingHeader.get("deliveryId") : "");
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
        try {
            // 添加空值检查
            if (request == null) {
                throw new RuntimeException("请求数据不能为空");
            }
            
            BillingEditRequest.Meta meta = request.getMeta();
            BillingEditRequest.BasicInfo basicInfo = request.getBasicInfo();
            BillingEditRequest.ItemOverview itemOverview = request.getItemOverview();
            
            if (meta == null) {
                throw new RuntimeException("meta信息不能为空");
            }
            
            String billingId = meta.getId();
            boolean isUpdate = billingId != null && !billingId.isEmpty();
            
            // 记录日志
            log.info("开始{}开票凭证，ID: {}", isUpdate ? "更新" : "创建", billingId);
            
            if (isUpdate) {
                // 更新现有开票凭证
                if (basicInfo == null) {
                    throw new RuntimeException("更新时basicInfo不能为空");
                }
                
                log.debug("更新开票凭证: {}", billingId);
                billingMapper.updateBilling(request);
                
                // 删除现有项目
                log.debug("删除开票凭证项目: {}", billingId);
                billingMapper.deleteBillingItems(billingId);
            } else {
                // 创建新开票凭证
                if (basicInfo == null) {
                    throw new RuntimeException("创建时basicInfo不能为空");
                }
                
                // 确保 payerId 存在
                if (basicInfo.getPayerId() == null || basicInfo.getPayerId().isEmpty()) {
                    // 如果没有 payerId，尝试从数据库获取客户ID
                    String deliveryId = basicInfo.getDeliveryId();
                    if (deliveryId != null && !deliveryId.isEmpty()) {
                        String customerId = billingMapper.getCustomerIdByDeliveryId(deliveryId);
                        if (customerId != null && !customerId.isEmpty()) {
                            basicInfo.setPayerId(customerId);
                        }
                    }
                }
                
                // 确保必要的字段有默认值
                if (basicInfo.getNetValue() == null) {
                    basicInfo.setNetValue("0.00");
                }
                if (basicInfo.getTaxValue() == null) {
                    basicInfo.setTaxValue("0.00");
                }
                if (basicInfo.getGrossValue() == null) {
                    basicInfo.setGrossValue("0.00");
                }
                
                log.debug("创建新开票凭证，交货单ID: {}", basicInfo.getDeliveryId());
                billingMapper.createBilling(request);
                
                // 获取生成的ID
                billingId = String.valueOf(request.getBasicInfo().getId());
                log.debug("生成开票凭证ID: {}", billingId);
            }
            
            // 插入项目
            if (itemOverview != null && itemOverview.getItems() != null) {
                List<BillingEditRequest.Item> items = itemOverview.getItems();
                if (!items.isEmpty()) {
                    log.debug("插入{}个项目", items.size());
                    for (int i = 0; i < items.size(); i++) {
                        BillingEditRequest.Item item = items.get(i);
                        if (item != null) {
                            // 添加项目号
                            Map<String, Object> itemMap = new HashMap<>();
                            itemMap.put("itemNo", i + 1);
                            itemMap.put("materialId", item.getMaterialId() != null ? item.getMaterialId() : "1");
                            itemMap.put("quantity", item.getQuantity() != null ? item.getQuantity() : "0");
                            itemMap.put("netPrice", item.getNetPrice() != null ? item.getNetPrice() : "0.00");
                            itemMap.put("taxRate", item.getTaxRate() != null ? item.getTaxRate() : "10");
                            
                            log.debug("插入项目 {}: {}", i + 1, itemMap);
                            billingMapper.insertBillingItem(billingId, itemMap);
                        }
                    }
                } else {
                    log.debug("没有项目需要插入");
                }
            } else {
                log.debug("itemOverview为空，没有项目需要插入");
            }
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> responseMeta = new HashMap<>();
            responseMeta.put("id", billingId != null ? billingId : "");
            result.put("meta", responseMeta);
            
            result.put("basicInfo", basicInfo != null ? basicInfo : new BillingEditRequest.BasicInfo());
            result.put("itemOverview", itemOverview != null ? itemOverview : new BillingEditRequest.ItemOverview());
            
            log.info("开票凭证{}成功，ID: {}", isUpdate ? "更新" : "创建", billingId);
            return result;
            
        } catch (Exception e) {
            log.error("保存开票凭证失败: ", e);
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
