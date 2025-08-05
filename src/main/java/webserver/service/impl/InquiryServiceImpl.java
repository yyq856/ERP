package webserver.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import webserver.mapper.InquiryMapper;
import webserver.pojo.*;
import webserver.service.InquiryService;

import java.time.LocalDate;

import java.util.*;

@Slf4j
@Service
public class InquiryServiceImpl implements InquiryService {

    @Autowired
    private InquiryMapper inquiryMapper;

    @Override
    public InquiryResponse initialize(InquiryInitializeRequest request) {
        try {
            // 参数验证
            if (!StringUtils.hasText(request.getInquiryType())) {
                return InquiryResponse.error("询价单类型不能为空");
            }
            
            // 生成默认值
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> itemOverview = new HashMap<>();
            
            // 设置默认的请求交货日期（30天后）
            LocalDate defaultReqDelivDate = LocalDate.now().plusDays(30);
            itemOverview.put("reqDelivDate", defaultReqDelivDate.toString());
            
            content.put("itemOverview", itemOverview);
            
            Map<String, Object> data = new HashMap<>();
            data.put("content", content);
            
            log.info("询价单初始化成功，类型: {}", request.getInquiryType());
            return InquiryResponse.success(data, "初始化询价单成功");
            
        } catch (Exception e) {
            log.error("询价单初始化异常: {}", e.getMessage(), e);
            return InquiryResponse.error("服务器内部错误");
        }
    }

    @Override
    public InquiryResponse get(InquiryGetRequest request) {
        try {
            // 参数验证
            if (!StringUtils.hasText(request.getInquiryId())) {
                return InquiryResponse.error("询价单ID不能为空");
            }
            
            Long inquiryId;
            try {
                inquiryId = Long.parseLong(request.getInquiryId());
            } catch (NumberFormatException e) {
                return InquiryResponse.error("询价单ID格式不正确");
            }
            
            // 查询询价单
            Inquiry inquiry = inquiryMapper.findByInquiryId(inquiryId);
            if (inquiry == null) {
                return InquiryResponse.error("询价单不存在");
            }
            
            // 查询询价单项目
            List<InquiryItem> items = inquiryMapper.findItemsByInquiryId(inquiryId);
            
            // 构建响应数据
            Map<String, Object> content = buildInquiryContent(inquiry, items);
            
            Map<String, Object> data = new HashMap<>();
            data.put("content", content);
            
            log.info("询价单查询成功，ID: {}", inquiryId);
            return InquiryResponse.success(data, "获取询价单成功");
            
        } catch (Exception e) {
            log.error("询价单查询异常: {}", e.getMessage(), e);
            return InquiryResponse.error("服务器内部错误");
        }
    }

    @Override
    public InquiryResponse edit(InquiryEditRequest request) {
        try {
            // 参数验证
            if (request.getBasicInfo() == null) {
                return InquiryResponse.error("基本信息不能为空");
            }
            
            // 构建询价单对象
            Inquiry inquiry = buildInquiryFromRequest(request);
            
            String message;
            String responseMessage;
            String inquiryCode;
            
            // 判断是创建还是更新
            String id = request.getMeta() != null ? request.getMeta().getId() : null;
            
            if (StringUtils.hasText(id)) {
                // 更新操作
                inquiry.setInquiryId(Long.parseLong(id));
                int result = inquiryMapper.updateInquiry(inquiry);
                if (result > 0) {
                    // 更新项目
                    updateInquiryItems(inquiry.getInquiryId(), request.getItemOverview());
                    inquiryCode = generateInquiryCode(inquiry.getInquiryId());
                    message = "Inquiry " + inquiryCode + " has been updated successfully";
                    responseMessage = "修改询价单成功";
                } else {
                    return InquiryResponse.error("更新失败");
                }
            } else {
                // 创建操作
                int result = inquiryMapper.insertInquiry(inquiry);
                if (result > 0) {
                    // 插入项目
                    insertInquiryItems(inquiry.getInquiryId(), request.getItemOverview());
                    inquiryCode = generateInquiryCode(inquiry.getInquiryId());
                    message = "Inquiry " + inquiryCode + " has been created successfully";
                    responseMessage = "创建询价单成功";
                    id = inquiry.getInquiryId().toString();
                } else {
                    return InquiryResponse.error("创建失败");
                }
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("message", message);
            
            if (!StringUtils.hasText(request.getMeta().getId())) {
                // 创建时返回ID
                Map<String, Object> content = new HashMap<>();
                content.put("id", inquiryCode);
                data.put("content", content);
            }
            
            log.info("询价单编辑成功，ID: {}", id);
            return InquiryResponse.success(data, responseMessage);
            
        } catch (Exception e) {
            log.error("询价单编辑异常: {}", e.getMessage(), e);
            return InquiryResponse.error("服务器内部错误");
        }
    }

    @Override
    public InquiryResponse itemsTabQuery(List<InquiryItemsTabQueryRequest.ItemQuery> items) {
        try {
            if (items == null || items.isEmpty()) {
                return InquiryResponse.error("查询项目不能为空");
            }
            
            // 构建响应数据
            Map<String, Object> data = buildItemsTabQueryResponse(items);
            
            log.info("物品批量查询成功，查询项目数: {}", items.size());
            return InquiryResponse.success(data, "批量查询成功");
            
        } catch (Exception e) {
            log.error("物品批量查询异常: {}", e.getMessage(), e);
            return InquiryResponse.error("服务器内部错误");
        }
    }
    
    /**
     * 构建询价单内容
     */
    private Map<String, Object> buildInquiryContent(Inquiry inquiry, List<InquiryItem> items) {
        Map<String, Object> content = new HashMap<>();
        
        // Meta信息
        Map<String, Object> meta = new HashMap<>();
        meta.put("id", generateInquiryCode(inquiry.getInquiryId()));
        content.put("meta", meta);
        
        // 基本信息
        Map<String, Object> basicInfo = new HashMap<>();
        basicInfo.put("inquiry", generateInquiryCode(inquiry.getInquiryId()));
        basicInfo.put("soldToParty", "CUST-" + inquiry.getSoldTp());
        basicInfo.put("shipToParty", "SHIP-" + inquiry.getShipTp());
        basicInfo.put("customerReference", inquiry.getCustRef());
        basicInfo.put("netValue", inquiry.getNetValue());
        basicInfo.put("netValueUnit", "USD");
        basicInfo.put("customerReferenceDate", inquiry.getCustomerReferenceDate() != null ? 
                     inquiry.getCustomerReferenceDate().toString() : null);
        content.put("basicInfo", basicInfo);
        
        // 项目概览
        Map<String, Object> itemOverview = new HashMap<>();
        itemOverview.put("validFrom", inquiry.getValidFromDate() != null ? 
                        inquiry.getValidFromDate().toString() : null);
        itemOverview.put("validTo", inquiry.getValidToDate() != null ? 
                        inquiry.getValidToDate().toString() : null);
        itemOverview.put("reqDelivDate", "2024-02-15"); // 示例值
        itemOverview.put("expectOralVal", "16000.00");
        itemOverview.put("expectOralValUnit", "USD");
        
        // 构建项目列表
        List<Map<String, Object>> itemList = new ArrayList<>();
        for (InquiryItem item : items) {
            Map<String, Object> itemMap = buildItemMap(item);
            itemList.add(itemMap);
        }
        itemOverview.put("items", itemList);
        content.put("itemOverview", itemOverview);
        
        return content;
    }
    
    /**
     * 构建项目映射
     */
    private Map<String, Object> buildItemMap(InquiryItem item) {
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("item", item.getItemNo().toString());
        itemMap.put("material", "MAT-" + item.getMatId());
        itemMap.put("orderQuantity", item.getQuantity().toString());
        itemMap.put("orderQuantityUnit", item.getSu());
        itemMap.put("description", "物料描述 " + item.getMatId());
        itemMap.put("reqDelivDate", "2024-02-15");
        itemMap.put("netValue", item.getNetPrice().toString());
        itemMap.put("netValueUnit", "USD");
        itemMap.put("taxValue", String.format("%.2f", item.getNetPrice() * 0.15));
        itemMap.put("taxValueUnit", "USD");
        itemMap.put("pricingDate", "2024-01-15");
        itemMap.put("orderProbability", "95");
        
        // 定价元素
        List<Map<String, Object>> pricingElements = new ArrayList<>();
        Map<String, Object> pricingElement = new HashMap<>();
        pricingElement.put("cnty", "US");
        pricingElement.put("name", "Base Price");
        pricingElement.put("amount", item.getNetPrice().toString());
        pricingElement.put("city", "USD");
        pricingElement.put("per", "1");
        pricingElement.put("uom", item.getSu());
        pricingElement.put("conditionValue", item.getNetPrice().toString());
        pricingElement.put("curr", "USD");
        pricingElement.put("status", "Active");
        pricingElement.put("numC", "1");
        pricingElement.put("atoMtsComponent", "");
        pricingElement.put("oun", "");
        pricingElement.put("cconDe", "");
        pricingElement.put("un", "");
        pricingElement.put("conditionValue2", item.getNetPrice().toString());
        pricingElement.put("cdCur", "USD");
        pricingElement.put("stat", true);
        pricingElements.add(pricingElement);
        
        itemMap.put("pricingElements", pricingElements);
        return itemMap;
    }
    
    /**
     * 从请求构建询价单对象
     */
    private Inquiry buildInquiryFromRequest(InquiryEditRequest request) {
        Inquiry inquiry = new Inquiry();
        
        InquiryEditRequest.BasicInfo basicInfo = request.getBasicInfo();
        inquiry.setCustId(1L); // 默认客户ID
        inquiry.setInquiryType("ZAG"); // 默认询价单类型
        inquiry.setSlsOrg("1000"); // 默认销售组织
        inquiry.setSalesDistrict("000001"); // 默认销售区域
        inquiry.setDivision("01"); // 默认产品线
        
        if (StringUtils.hasText(basicInfo.getSoldToParty())) {
            try {
                String soldToPartyId = basicInfo.getSoldToParty().replace("CUST-", "");
                inquiry.setSoldTp(Long.parseLong(soldToPartyId));
            } catch (Exception e) {
                inquiry.setSoldTp(1L);
            }
        } else {
            inquiry.setSoldTp(1L);
        }
        
        if (StringUtils.hasText(basicInfo.getShipToParty())) {
            try {
                String shipToPartyId = basicInfo.getShipToParty().replace("SHIP-", "");
                inquiry.setShipTp(Long.parseLong(shipToPartyId));
            } catch (Exception e) {
                inquiry.setShipTp(1L);
            }
        } else {
            inquiry.setShipTp(1L);
        }
        
        inquiry.setCustRef(basicInfo.getCustomerReference());
        inquiry.setNetValue(basicInfo.getNetValue() != null ? basicInfo.getNetValue().floatValue() : 0.0f);
        
        if (StringUtils.hasText(basicInfo.getCustomerReferenceDate())) {
            try {
                inquiry.setCustomerReferenceDate(LocalDate.parse(basicInfo.getCustomerReferenceDate()));
            } catch (Exception e) {
                inquiry.setCustomerReferenceDate(LocalDate.now());
            }
        }
        
        // 设置有效期
        if (request.getItemOverview() != null) {
            if (StringUtils.hasText(request.getItemOverview().getValidFrom())) {
                try {
                    inquiry.setValidFromDate(LocalDate.parse(request.getItemOverview().getValidFrom()));
                } catch (Exception e) {
                    inquiry.setValidFromDate(LocalDate.now());
                }
            }
            
            if (StringUtils.hasText(request.getItemOverview().getValidTo())) {
                try {
                    inquiry.setValidToDate(LocalDate.parse(request.getItemOverview().getValidTo()));
                } catch (Exception e) {
                    inquiry.setValidToDate(LocalDate.now().plusMonths(6));
                }
            }
        }
        
        inquiry.setProbability(95.0f); // 默认概率
        inquiry.setStatus("OPEN"); // 默认状态
        
        return inquiry;
    }
    
    /**
     * 插入询价单项目
     */
    private void insertInquiryItems(Long inquiryId, InquiryEditRequest.ItemOverview itemOverview) {
        if (itemOverview == null || itemOverview.getItems() == null) {
            return;
        }
        
        for (int i = 0; i < itemOverview.getItems().size(); i++) {
            InquiryEditRequest.InquiryItemDetail itemDetail = itemOverview.getItems().get(i);
            InquiryItem item = new InquiryItem();
            item.setInquiryId(inquiryId);
            item.setItemNo(i + 1);
            
            // 解析物料ID - 使用现有的物料ID或默认值
            if (StringUtils.hasText(itemDetail.getMaterial())) {
                try {
                    String matId = itemDetail.getMaterial().replace("MAT-", "");
                    item.setMatId(Long.parseLong(matId));
                } catch (Exception e) {
                    // 如果解析失败，先查询数据库中是否有可用的物料ID
                    // 这里暂时使用一个可能存在的ID，实际应该查询数据库
                    item.setMatId(1L);
                }
            } else {
                // 默认使用第一个可用的物料ID
                item.setMatId(1L);
            }
            
            // 设置数量和价格
            try {
                item.setQuantity(Integer.parseInt(itemDetail.getOrderQuantity()));
            } catch (Exception e) {
                item.setQuantity(1);
            }
            
            try {
                item.setNetPrice(Float.parseFloat(itemDetail.getNetValue()));
                item.setItemValue(item.getNetPrice() * item.getQuantity());
            } catch (Exception e) {
                item.setNetPrice(0.0f);
                item.setItemValue(0.0f);
            }
            
            item.setPlantId(1000L); // 默认工厂
            item.setSu(StringUtils.hasText(itemDetail.getOrderQuantityUnit()) ? 
                      itemDetail.getOrderQuantityUnit() : "EA");
            
            inquiryMapper.insertInquiryItem(item);
        }
    }
    
    /**
     * 更新询价单项目
     */
    private void updateInquiryItems(Long inquiryId, InquiryEditRequest.ItemOverview itemOverview) {
        // 先删除所有现有项目
        inquiryMapper.deleteInquiryItems(inquiryId);
        
        // 重新插入项目
        insertInquiryItems(inquiryId, itemOverview);
    }
    
    /**
     * 构建物品批量查询响应
     */
    private Map<String, Object> buildItemsTabQueryResponse(List<InquiryItemsTabQueryRequest.ItemQuery> items) {
        Map<String, Object> data = new HashMap<>();
        
        // 汇总信息
        Map<String, Object> summary = new HashMap<>();
        double totalNetValue = 0.0;
        double totalExpectOralVal = 0.0;
        
        // 明细信息
        List<Map<String, Object>> breakdowns = new ArrayList<>();
        
        for (InquiryItemsTabQueryRequest.ItemQuery item : items) {
            Map<String, Object> breakdown = new HashMap<>();
            breakdown.put("item", item.getItem());
            breakdown.put("material", item.getMaterial());
            breakdown.put("orderQuantity", item.getOrderQuantity());
            breakdown.put("orderQuantityUnit", item.getOrderQuantityUnit());
            breakdown.put("description", item.getDescription());
            breakdown.put("reqDelivDate", item.getReqDelivDate());
            
            // 计算价格
            double netValue = 0.0;
            try {
                netValue = Double.parseDouble(item.getNetValue());
            } catch (Exception e) {
                netValue = 1500.0; // 默认价格
            }
            
            breakdown.put("netValue", netValue);
            breakdown.put("netValueUnit", "USD");
            breakdown.put("taxValue", netValue * 0.15);
            breakdown.put("taxValueUnit", "USD");
            breakdown.put("pricingDate", item.getPricingDate());
            breakdown.put("orderProbability", item.getOrderProbability());
            
            // 定价元素
            List<Map<String, Object>> pricingElements = new ArrayList<>();
            Map<String, Object> pricingElement = new HashMap<>();
            pricingElement.put("cnty", "US");
            pricingElement.put("name", "Base Price");
            pricingElement.put("amount", String.valueOf(netValue));
            pricingElement.put("city", "USD");
            pricingElement.put("per", "1");
            pricingElement.put("uom", item.getOrderQuantityUnit());
            pricingElement.put("conditionValue", String.valueOf(netValue));
            pricingElement.put("curr", "USD");
            pricingElement.put("status", "Active");
            pricingElement.put("numC", "1");
            pricingElement.put("atoMtsComponent", "");
            pricingElement.put("oun", "");
            pricingElement.put("cconDe", "");
            pricingElement.put("un", "");
            pricingElement.put("conditionValue2", String.valueOf(netValue));
            pricingElement.put("cdCur", "USD");
            pricingElement.put("stat", true);
            pricingElements.add(pricingElement);
            
            breakdown.put("pricingElements", pricingElements);
            breakdowns.add(breakdown);
            
            totalNetValue += netValue;
            totalExpectOralVal += netValue * 1.1; // 期望值比净值高10%
        }
        
        summary.put("totalNetValue", totalNetValue);
        summary.put("totalExpectOralVal", totalExpectOralVal);
        summary.put("currency", "USD");
        
        data.put("summary", summary);
        data.put("breakdowns", breakdowns);
        data.put("badRecordIndices", new ArrayList<>());
        
        return data;
    }
    
    /**
     * 生成询价单编号
     */
    private String generateInquiryCode(Long inquiryId) {
        return String.format("INQ-2024-%03d", inquiryId);
    }
}
