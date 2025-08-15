package webserver.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import webserver.mapper.BillingMapper;
import webserver.pojo.*;
import webserver.service.BillingService;
import webserver.service.UnifiedItemService;
import webserver.service.ValidateItemsService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Service
public class BillingServiceImpl implements BillingService {
    
    private static final Logger log = LoggerFactory.getLogger(BillingServiceImpl.class);
    
    @Autowired
    private BillingMapper billingMapper;

    @Autowired
    private UnifiedItemService unifiedItemService;

    @Autowired
    private ValidateItemsService validateItemsService;
    
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
            // 首先获取交货单对应的销售订单ID
            Map<String, Object> deliveryInfo = billingMapper.getDeliveryById(deliveryId);
            if (deliveryInfo != null) {
                Object salesOrderIdObj = deliveryInfo.get("salesOrderId");
                if (salesOrderIdObj != null) {
                    try {
                        Long soId;
                        if (salesOrderIdObj instanceof Long) {
                            soId = (Long) salesOrderIdObj;
                        } else {
                            soId = Long.parseLong(salesOrderIdObj.toString());
                        }
                        // 使用统一item服务获取销售订单的items（包含完整的pricingElements）
                        items = unifiedItemService.getDocumentItemsAsFrontendFormat(soId, "sales_order");
                        log.info("从统一item服务获取到 {} 个items", items.size());
                    } catch (Exception e) {
                        log.warn("销售订单ID格式错误: {}, 错误: {}", salesOrderIdObj, e.getMessage());
                        items = new ArrayList<>();
                    }
                } else {
                    log.warn("无法获取交货单 {} 对应的销售订单ID", deliveryId);
                    items = new ArrayList<>();
                }
            } else {
                log.warn("无法找到交货单: {}", deliveryId);
                items = new ArrayList<>();
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
        
        // 使用统一item服务获取开票凭证项目信息
        List<Map<String, Object>> billingItems;
        try {
            Long documentId = Long.parseLong(billingDocumentId);
            billingItems = unifiedItemService.getDocumentItemsAsFrontendFormat(documentId, "billing_doc");
            log.info("从统一item服务获取到开票凭证{}的{}个items", billingDocumentId, billingItems.size());
        } catch (Exception e) {
            log.warn("使用统一item服务获取开票凭证items失败，回退到旧方式: {}", e.getMessage());
            // 回退到旧方式
            billingItems = billingMapper.getBillingItemsById(billingDocumentId);

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
        // 添加deliveryId字段
        basicInfo.put("deliveryId", billingHeader.get("deliveryId") != null ? billingHeader.get("deliveryId") : "");
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
                
                // 处理netValue中的逗号分隔符
                if (basicInfo.getNetValue() != null) {
                    basicInfo.setNetValue(basicInfo.getNetValue().replace(",", ""));
                }
                if (basicInfo.getTaxValue() != null) {
                    basicInfo.setTaxValue(basicInfo.getTaxValue().replace(",", ""));
                }
                if (basicInfo.getGrossValue() != null) {
                    basicInfo.setGrossValue(basicInfo.getGrossValue().replace(",", ""));
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
                    log.info("尝试从交货单 {} 获取客户ID", deliveryId);
                    if (deliveryId != null && !deliveryId.isEmpty()) {
                        String customerId = billingMapper.getCustomerIdByDeliveryId(deliveryId);
                        log.info("从交货单 {} 获取到客户ID: {}", deliveryId, customerId);
                        if (customerId != null && !customerId.isEmpty()) {
                            basicInfo.setPayerId(customerId);
                            log.info("设置payerId为: {}", customerId);
                        } else {
                            log.warn("无法从交货单 {} 获取客户ID，使用默认值", deliveryId);
                            basicInfo.setPayerId("1"); // 设置默认客户ID
                        }
                    } else {
                        log.warn("deliveryId为空，使用默认客户ID");
                        basicInfo.setPayerId("1"); // 设置默认客户ID
                    }
                } else {
                    log.info("使用提供的payerId: {}", basicInfo.getPayerId());
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
            
            // 插入项目到erp_billing_item表
            if (itemOverview != null && itemOverview.getItems() != null) {
                List<BillingEditRequest.Item> items = itemOverview.getItems();
                if (!items.isEmpty()) {
                    log.debug("插入{}个项目到erp_billing_item表", items.size());
                    int validItemNo = 1; // 有效项目的序号
                    for (int i = 0; i < items.size(); i++) {
                        BillingEditRequest.Item item = items.get(i);
                        if (item != null) {
                            // 过滤空的item - 检查material字段是否有效
                            if (item.getMaterial() == null || item.getMaterial().trim().isEmpty()) {
                                log.debug("跳过空的item: index={}, material为空", i + 1);
                                continue;
                            }

                            // 添加项目号
                            Map<String, Object> itemMap = new HashMap<>();
                            itemMap.put("itemNo", validItemNo);
                            itemMap.put("materialId", item.getMaterialId() != null ? item.getMaterialId() : "1");
                            itemMap.put("quantity", item.getQuantity() != null ? item.getQuantity() : "0");
                            itemMap.put("netPrice", item.getNetPrice() != null ? item.getNetPrice() : "0.00");
                            itemMap.put("taxRate", item.getTaxRate() != null ? item.getTaxRate() : "10");

                            log.debug("插入项目 {}: {}", validItemNo, itemMap);
                            billingMapper.insertBillingItem(billingId, itemMap);
                            validItemNo++; // 只有有效项目才增加序号
                        }
                    }

                    // 🔥 调用统一item服务，将数据写入erp_item表
                    try {
                        Long documentId = Long.parseLong(billingId);
                        List<Map<String, Object>> unifiedItems = convertBillingItemsToUnifiedFormat(items);
                        unifiedItemService.updateDocumentItems(documentId, "billing_doc", unifiedItems);
                        log.info("成功调用统一item服务，写入{}个items到erp_item表", unifiedItems.size());
                    } catch (Exception e) {
                        log.error("调用统一item服务失败: {}", e.getMessage(), e);
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
    public Map<String, Object> validateBillingItems(List<ItemValidationRequest> items) {
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
            ItemValidationRequest item = items.get(i);
            
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
                    for (ItemValidationRequest.PricingElementRequest element : item.getPricingElements()) {
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

    @Override
    public BillingResponse itemsTabQuery(List<BillingItemsTabQueryRequest.ItemQuery> items) {
        try {
            if (items == null || items.isEmpty()) {
                return BillingResponse.error("查询项目不能为空");
            }

            // 转换为 ItemValidationRequest 格式并调用验证服务（复用inquiry的逻辑）
            List<ItemValidationRequest> validationRequests = convertBillingItemsToValidationRequests(items);
            ItemValidationResponse validationResponse = validateItemsService.validateItems(validationRequests);

            // 将验证结果转换为 Billing 的响应格式
            Map<String, Object> data = convertValidationResponseToBillingFormat(validationResponse);

            log.info("物品批量查询成功，查询项目数: {}", items.size());
            return BillingResponse.success(data, "批量查询成功");

        } catch (Exception e) {
            log.error("物品批量查询异常: {}", e.getMessage(), e);
            return BillingResponse.error("服务器内部错误");
        }
    }

    /**
     * 转换 BillingItemsTabQueryRequest.ItemQuery 到 ItemValidationRequest
     */
    private List<ItemValidationRequest> convertBillingItemsToValidationRequests(List<BillingItemsTabQueryRequest.ItemQuery> items) {
        List<ItemValidationRequest> validationRequests = new ArrayList<>();

        for (BillingItemsTabQueryRequest.ItemQuery queryItem : items) {
            ItemValidationRequest request = new ItemValidationRequest();
            request.setItem(queryItem.getItem());
            request.setMaterial(queryItem.getMaterial());
            request.setOrderQuantity(queryItem.getOrderQuantity());
            request.setOrderQuantityUnit(queryItem.getOrderQuantityUnit());
            request.setDescription(queryItem.getDescription());
            request.setReqDelivDate(queryItem.getReqDelivDate());
            request.setNetValue(queryItem.getNetValue());
            request.setNetValueUnit(queryItem.getNetValueUnit());
            request.setTaxValue(queryItem.getTaxValue());
            request.setTaxValueUnit(queryItem.getTaxValueUnit());
            request.setPricingDate(queryItem.getPricingDate());
            request.setOrderProbability(queryItem.getOrderProbability());
            validationRequests.add(request);
        }

        return validationRequests;
    }

    /**
     * 将验证结果转换为 Billing 的响应格式
     */
    private Map<String, Object> convertValidationResponseToBillingFormat(ItemValidationResponse validationResponse) {
        Map<String, Object> data = new HashMap<>();

        if (validationResponse != null && validationResponse.getData() != null) {
            // 转换验证结果
            if (validationResponse.getData().getResult() != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("allDataLegal", validationResponse.getData().getResult().getAllDataLegal());
                result.put("badRecordIndices", validationResponse.getData().getResult().getBadRecordIndices());
                data.put("result", result);
            }

            // 转换汇总信息
            if (validationResponse.getData().getGeneralData() != null) {
                Map<String, Object> summary = new HashMap<>();
                summary.put("totalNetValue", validationResponse.getData().getGeneralData().getNetValue());
                summary.put("totalNetValueUnit", validationResponse.getData().getGeneralData().getNetValueUnit());
                summary.put("totalExpectOralVal", validationResponse.getData().getGeneralData().getExpectOralVal());
                summary.put("totalExpectOralValUnit", validationResponse.getData().getGeneralData().getExpectOralValUnit());
                data.put("summary", summary);
            }

            // 转换明细列表
            if (validationResponse.getData().getBreakdowns() != null) {
                List<Map<String, Object>> breakdowns = new ArrayList<>();
                for (ItemValidationResponse.ItemBreakdown breakdown : validationResponse.getData().getBreakdowns()) {
                    Map<String, Object> breakdownMap = new HashMap<>();
                    breakdownMap.put("item", breakdown.getItem());
                    breakdownMap.put("material", breakdown.getMaterial());
                    breakdownMap.put("orderQuantity", breakdown.getOrderQuantity());
                    breakdownMap.put("orderQuantityUnit", breakdown.getOrderQuantityUnit());
                    breakdownMap.put("description", breakdown.getDescription());
                    breakdownMap.put("reqDelivDate", breakdown.getReqDelivDate());
                    breakdownMap.put("netValue", breakdown.getNetValue());
                    breakdownMap.put("netValueUnit", breakdown.getNetValueUnit());
                    breakdownMap.put("taxValue", breakdown.getTaxValue());
                    breakdownMap.put("taxValueUnit", breakdown.getTaxValueUnit());
                    breakdownMap.put("pricingDate", breakdown.getPricingDate());
                    breakdownMap.put("orderProbability", breakdown.getOrderProbability());
                    breakdownMap.put("pricingElements", breakdown.getPricingElements());
                    breakdowns.add(breakdownMap);
                }
                data.put("breakdowns", breakdowns);
            }
        }

        return data;
    }

    /**
     * 将BillingEditRequest.Item转换为统一的前端数据格式
     * 参考inquiry的实现方式
     */
    /**
     * 转换开票凭证项目为统一item服务格式（用于存储到数据库）
     */
    private List<Map<String, Object>> convertBillingItemsToUnifiedFormat(List<BillingEditRequest.Item> items) {
        List<Map<String, Object>> unifiedItems = new ArrayList<>();

        for (BillingEditRequest.Item item : items) {
            // 过滤空的item - 检查material字段是否有效
            if (item.getMaterial() == null || item.getMaterial().trim().isEmpty()) {
                log.debug("跳过空的item: material为空");
                continue;
            }

            Map<String, Object> unifiedItem = new HashMap<>();

            // 基本字段
            unifiedItem.put("item", item.getItem() != null ? item.getItem() : "");
            unifiedItem.put("material", item.getMaterial());
            unifiedItem.put("orderQuantity", item.getOrderQuantity() != null ? item.getOrderQuantity() : "1");
            unifiedItem.put("orderQuantityUnit", item.getOrderQuantityUnit() != null ? item.getOrderQuantityUnit() : "EA");
            unifiedItem.put("description", item.getDescription() != null ? item.getDescription() : "");
            unifiedItem.put("reqDelivDate", item.getReqDelivDate() != null ? item.getReqDelivDate() : "");
            unifiedItem.put("netValue", item.getNetValue() != null ? item.getNetValue().toString() : "0");
            unifiedItem.put("netValueUnit", item.getNetValueUnit() != null ? item.getNetValueUnit() : "CNY");
            unifiedItem.put("taxValue", item.getTaxValue() != null ? item.getTaxValue().toString() : "0");
            unifiedItem.put("taxValueUnit", item.getTaxValueUnit() != null ? item.getTaxValueUnit() : "CNY");
            unifiedItem.put("pricingDate", item.getPricingDate() != null ? item.getPricingDate() : "");
            unifiedItem.put("orderProbability", item.getOrderProbability() != null ? item.getOrderProbability() : "100");

            // 处理定价元素 - 直接传递List对象给统一item服务
            if (item.getPricingElements() != null && !item.getPricingElements().isEmpty()) {
                // 直接传递List对象，统一item服务会自动序列化为JSON字符串
                unifiedItem.put("pricingElements", item.getPricingElements());
                log.debug("统一格式pricingElements: {} 个元素", item.getPricingElements().size());
            } else {
                // 创建默认的定价元素List
                List<Map<String, Object>> defaultPricingElements = new ArrayList<>();
                Map<String, Object> defaultElement = new HashMap<>();
                defaultElement.put("cnty", "BASE");
                defaultElement.put("name", "基本价格");
                defaultElement.put("amount", item.getNetValue() != null ? item.getNetValue().toString() : "0.00");
                defaultElement.put("city", "CNY");
                defaultElement.put("per", "1");
                defaultElement.put("uom", "EA");
                defaultElement.put("conditionValue", item.getNetValue() != null ? item.getNetValue().toString() : "0.00");
                defaultElement.put("curr", "CNY");
                defaultElement.put("status", "");
                defaultElement.put("numC", "");
                defaultElement.put("atoMtsComponent", "");
                defaultElement.put("oun", "");
                defaultElement.put("cconDe", "");
                defaultElement.put("un", "");
                defaultElement.put("conditionValue2", "");
                defaultElement.put("cdCur", "");
                defaultElement.put("stat", true);
                defaultPricingElements.add(defaultElement);

                unifiedItem.put("pricingElements", defaultPricingElements);
                log.debug("默认统一格式pricingElements: 1 个默认元素");
            }

            unifiedItems.add(unifiedItem);
        }

        return unifiedItems;
    }

    /**
     * 转换开票凭证项目为前端格式（用于返回给前端）
     */
    private List<Map<String, Object>> convertBillingItemsToFrontendFormat(List<BillingEditRequest.Item> items) {
        List<Map<String, Object>> frontendItems = new ArrayList<>();

        for (BillingEditRequest.Item item : items) {
            // 过滤空的item - 检查material字段是否有效
            if (item.getMaterial() == null || item.getMaterial().trim().isEmpty()) {
                log.debug("跳过空的item: material为空");
                continue;
            }

            Map<String, Object> frontendItem = new HashMap<>();

            // 基础字段 - 只使用BillingEditRequest.Item中实际存在的字段
            frontendItem.put("item", item.getItem());
            frontendItem.put("material", item.getMaterial());
            frontendItem.put("orderQuantity", item.getOrderQuantity());
            frontendItem.put("orderQuantityUnit", item.getOrderQuantityUnit());
            frontendItem.put("description", item.getDescription());
            frontendItem.put("reqDelivDate", item.getReqDelivDate());
            frontendItem.put("netValue", item.getNetValue());
            frontendItem.put("netValueUnit", item.getNetValueUnit());
            frontendItem.put("taxValue", item.getTaxValue());
            frontendItem.put("taxValueUnit", item.getTaxValueUnit());
            frontendItem.put("pricingDate", item.getPricingDate());
            frontendItem.put("orderProbability", item.getOrderProbability());

            // 处理定价元素 - 直接使用对象数组，但需要转换为JSON字符串存储到数据库
            log.debug("处理item的pricingElements: {}", item.getPricingElements());
            if (item.getPricingElements() != null && !item.getPricingElements().isEmpty()) {
                try {
                    // 将PricingElement列表转换为JSON字符串存储到数据库
                    ObjectMapper objectMapper = new ObjectMapper();
                    String pricingElementsJson = objectMapper.writeValueAsString(item.getPricingElements());
                    log.debug("序列化后的pricingElements: {}", pricingElementsJson);
                    frontendItem.put("pricingElements", pricingElementsJson);
                } catch (Exception e) {
                    log.warn("序列化定价元素失败: {}", e.getMessage());
                    frontendItem.put("pricingElements", "[]");
                }
            } else {
                log.debug("pricingElements为空，创建默认值");
                // 创建默认的定价元素JSON字符串
                String defaultPricingElements = String.format(
                    "[{\"cnty\":\"BASE\",\"name\":\"基本价格\",\"amount\":\"%s\",\"city\":\"CNY\",\"per\":\"1\",\"uom\":\"EA\",\"conditionValue\":\"%s\",\"curr\":\"CNY\",\"status\":\"\",\"numC\":\"\",\"atoMtsComponent\":\"\",\"oun\":\"\",\"cconDe\":\"\",\"un\":\"\",\"conditionValue2\":\"\",\"cdCur\":\"\",\"stat\":true}]",
                    item.getNetPrice() != null ? item.getNetPrice() : "0.00",
                    item.getNetValue() != null ? item.getNetValue() : "0.00"
                );
                log.debug("默认pricingElements: {}", defaultPricingElements);
                frontendItem.put("pricingElements", defaultPricingElements);
            }

            // 其他必需字段 - 使用默认值
            frontendItem.put("status", "");
            frontendItem.put("numC", "");
            frontendItem.put("atoMtsComponent", "");
            frontendItem.put("oun", "");
            frontendItem.put("cconDe", "");
            frontendItem.put("un", "");
            frontendItem.put("conditionValue2", "");
            frontendItem.put("cdCur", "");
            frontendItem.put("stat", true);

            frontendItems.add(frontendItem);
        }

        return frontendItems;
    }
}
