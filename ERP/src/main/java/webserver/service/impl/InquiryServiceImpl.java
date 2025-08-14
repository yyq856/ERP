package webserver.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import webserver.mapper.InquiryMapper;
import webserver.pojo.*;
import webserver.service.InquiryService;
import webserver.service.ValidateItemsService;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class InquiryServiceImpl implements InquiryService {

    @Autowired
    private InquiryMapper inquiryMapper;
    
    @Autowired
    private ValidateItemsService validateItemsService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

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

            // 初始化items为空列表，用户自行填写
            List<Map<String, Object>> items = new ArrayList<>();
            itemOverview.put("items", items);

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
            
            // 解析询价单ID - 支持编号格式（INQ-2024-001）或直接数字
            Long inquiryId = parseInquiryId(request.getInquiryId());
            if (inquiryId == null) {
                return InquiryResponse.error("询价单ID格式不正确");
            }
            
            // 查询询价单
            Inquiry inquiry = inquiryMapper.findByInquiryId(inquiryId);
            if (inquiry == null) {
                return InquiryResponse.error("询价单不存在");
            }
            
            // 查询询价单项目
            List<InquiryItem> items = inquiryMapper.findItemsByInquiryId(inquiryId);
            
            // 构建响应数据 - 使用完整的 ItemValidation 字段结构
            Map<String, Object> content = buildInquiryContentWithFullFields(inquiry, items);
            
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
                // 更新操作 - 解析询价单编号或直接使用数字ID
                Long inquiryId = parseInquiryId(id);
                if (inquiryId == null) {
                    return InquiryResponse.error("询价单ID解析失败：" + id);
                }
                
                log.info("更新询价单，解析ID: {} -> {}", id, inquiryId);
                
                // 验证询价单是否存在
                Inquiry existingInquiry = inquiryMapper.findByInquiryId(inquiryId);
                if (existingInquiry == null) {
                    return InquiryResponse.error("询价单不存在：" + id);
                }
                
                inquiry.setInquiryId(inquiryId);
                int result = inquiryMapper.updateInquiry(inquiry);
                if (result > 0) {
                    // 更新项目 - 使用完整的 ItemValidation 字段结构
                    updateInquiryItemsWithFullFields(inquiry.getInquiryId(), request.getItemOverview());
                    inquiryCode = generateInquiryCode(inquiry.getInquiryId());
                    message = "Inquiry " + inquiryCode + " has been updated successfully";
                    responseMessage = "修改询价单成功";
                } else {
                    return InquiryResponse.error("更新失败，影响行数：" + result);
                }
            } else {
                // 创建操作
                int result = inquiryMapper.insertInquiry(inquiry);
                if (result > 0) {
                    // 插入项目 - 使用完整的 ItemValidation 字段结构
                    insertInquiryItemsWithFullFields(inquiry.getInquiryId(), request.getItemOverview());
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
            
            // 转换为 ItemValidationRequest 格式并调用验证服务
            List<ItemValidationRequest> validationRequests = convertToItemValidationRequests(items);
            ItemValidationResponse validationResponse = validateItemsService.validateItems(validationRequests);
            
            // 将验证结果转换为 Inquiry 的响应格式
            Map<String, Object> data = convertValidationResponseToInquiryFormat(validationResponse);
            
            log.info("物品批量查询成功，查询项目数: {}", items.size());
            return InquiryResponse.success(data, "批量查询成功");
            
        } catch (Exception e) {
            log.error("物品批量查询异常: {}", e.getMessage(), e);
            return InquiryResponse.error("服务器内部错误");
        }
    }
    
    /**
     * 构建询价单内容 - 使用完整的 ItemValidation 字段结构
     */
    private Map<String, Object> buildInquiryContentWithFullFields(Inquiry inquiry, List<InquiryItem> items) {
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
        
        // 项目概览 - 计算汇总数据
        Map<String, Object> itemOverview = new HashMap<>();
        itemOverview.put("validFrom", inquiry.getValidFromDate() != null ? 
                        inquiry.getValidFromDate().toString() : null);
        itemOverview.put("validTo", inquiry.getValidToDate() != null ? 
                        inquiry.getValidToDate().toString() : null);
        
        // 从项目数据中计算期望口头值
        double totalExpectOralVal = 0.0;
        for (InquiryItem item : items) {
            if (item.getItemValue() != null) {
                totalExpectOralVal += item.getItemValue() * 1.1; // 期望值比净值高10%
            }
        }
        
    itemOverview.put("reqDelivDate", inquiry.getReqDelivDate() != null ? inquiry.getReqDelivDate().toString() : null);
        itemOverview.put("expectOralVal", String.format("%.2f", totalExpectOralVal));
        itemOverview.put("expectOralValUnit", "USD");
        
        // 构建完整的项目列表 - 使用 ItemValidation 字段结构
        List<Map<String, Object>> itemList = new ArrayList<>();
        for (InquiryItem item : items) {
            Map<String, Object> itemMap = buildFullItemMap(item);
            itemList.add(itemMap);
        }
        itemOverview.put("items", itemList);
        content.put("itemOverview", itemOverview);
        
        return content;
    }
    
    /**
     * 构建完整的项目映射 - 优先使用数据库存储的完整字段
     */
    private Map<String, Object> buildFullItemMap(InquiryItem item) {
        Map<String, Object> itemMap = new HashMap<>();
        
        // 优先使用数据库中存储的完整字段，如果为空才使用兼容字段或默认值
        itemMap.put("item", StringUtils.hasText(item.getItemCode()) ? item.getItemCode() : item.getItemNo().toString());
        itemMap.put("material", StringUtils.hasText(item.getMaterialCode()) ? item.getMaterialCode() : "MAT-" + item.getMatId());
        itemMap.put("orderQuantity", StringUtils.hasText(item.getOrderQuantityStr()) ? item.getOrderQuantityStr() : item.getQuantity().toString());
        itemMap.put("orderQuantityUnit", StringUtils.hasText(item.getOrderQuantityUnit()) ? item.getOrderQuantityUnit() : item.getSu());
        itemMap.put("description", StringUtils.hasText(item.getDescription()) ? item.getDescription() : "物料描述 " + item.getMatId());
    itemMap.put("reqDelivDate", StringUtils.hasText(item.getReqDelivDate()) ? item.getReqDelivDate() : null);
        itemMap.put("netValue", StringUtils.hasText(item.getNetValueStr()) ? item.getNetValueStr() : item.getNetPrice().toString());
        itemMap.put("netValueUnit", StringUtils.hasText(item.getNetValueUnit()) ? item.getNetValueUnit() : "USD");
        itemMap.put("taxValue", StringUtils.hasText(item.getTaxValueStr()) ? item.getTaxValueStr() : String.format("%.2f", item.getNetPrice() * 0.13));
        itemMap.put("taxValueUnit", StringUtils.hasText(item.getTaxValueUnit()) ? item.getTaxValueUnit() : "USD");
        itemMap.put("pricingDate", StringUtils.hasText(item.getPricingDate()) ? item.getPricingDate() : LocalDate.now().toString());
        itemMap.put("orderProbability", StringUtils.hasText(item.getOrderProbability()) ? item.getOrderProbability() : "95");
        
        // 处理定价元素
        List<Map<String, Object>> pricingElements = new ArrayList<>();
        if (StringUtils.hasText(item.getPricingElementsJson())) {
            try {
                List<Map<String, Object>> storedElements = objectMapper.readValue(
                    item.getPricingElementsJson(), 
                    new TypeReference<List<Map<String, Object>>>() {}
                );
                pricingElements = storedElements;
            } catch (Exception e) {
                log.warn("解析定价元素JSON失败: {}", e.getMessage());
            }
        }
        
        // 如果没有存储的定价元素，生成默认的
        if (pricingElements.isEmpty()) {
            Map<String, Object> defaultElement = new HashMap<>();
            defaultElement.put("cnty", "BASE");
            defaultElement.put("name", "Base Price");
            defaultElement.put("amount", item.getNetPrice().toString());
            defaultElement.put("city", "USD");
            defaultElement.put("per", "1");
            defaultElement.put("uom", item.getSu());
            defaultElement.put("conditionValue", item.getNetPrice().toString());
            defaultElement.put("curr", "USD");
            defaultElement.put("status", "Active");
            defaultElement.put("numC", "");
            defaultElement.put("atoMtsComponent", "");
            defaultElement.put("oun", "");
            defaultElement.put("cconDe", "");
            defaultElement.put("un", "");
            defaultElement.put("conditionValue2", "");
            defaultElement.put("cdCur", "");
            defaultElement.put("stat", true);
            pricingElements.add(defaultElement);
        }
        
        itemMap.put("pricingElements", pricingElements);
        return itemMap;
    }
    
    /**
     * 从请求构建询价单对象
     */
    private Inquiry buildInquiryFromRequest(InquiryEditRequest request) {
        Inquiry inquiry = new Inquiry();
        // 送货日期：只认 itemOverview.reqDelivDate，主表和itemOverview保持一致
        String reqDelivDate = null;
        if (request.getItemOverview() != null && org.springframework.util.StringUtils.hasText(request.getItemOverview().getReqDelivDate())) {
            reqDelivDate = request.getItemOverview().getReqDelivDate();
        }
        if (org.springframework.util.StringUtils.hasText(reqDelivDate)) {
            inquiry.setReqDelivDate(webserver.util.DateUtil.parseDateSafely(reqDelivDate));
        } else {
            inquiry.setReqDelivDate(null);
        }
        InquiryEditRequest.BasicInfo basicInfo = request.getBasicInfo();
        // 支持主表所有字段的更新，优先取前端传递值，否则用默认
        inquiry.setCustId(1L); // 你可根据实际需求调整
        inquiry.setInquiryType("ZAG");
        inquiry.setSlsOrg("1000");
        inquiry.setSalesDistrict("000001");
        inquiry.setDivision("01");

        if (basicInfo != null) {
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

            // 新增支持 netValueUnit
            if (StringUtils.hasText(basicInfo.getNetValueUnit())) {
                // 你可根据表结构扩展 netValueUnit 字段
            }

            if (StringUtils.hasText(basicInfo.getCustomerReferenceDate())) {
                try {
                    inquiry.setCustomerReferenceDate(webserver.util.DateUtil.parseDate(basicInfo.getCustomerReferenceDate()));
                } catch (Exception e) {
                    inquiry.setCustomerReferenceDate(LocalDate.now());
                }
            }
        }

        // 设置有效期，优先从 basicInfo 取（如有），否则从 itemOverview 取
        String validFrom = null;
        String validTo = null;
        if (basicInfo != null) {
            // 允许前端 future 扩展 basicInfo 直接传 validFrom/validTo
            try {
                java.lang.reflect.Field f1 = basicInfo.getClass().getDeclaredField("validFrom");
                f1.setAccessible(true);
                validFrom = (String) f1.get(basicInfo);
            } catch (Exception ignore) {}
            try {
                java.lang.reflect.Field f2 = basicInfo.getClass().getDeclaredField("validTo");
                f2.setAccessible(true);
                validTo = (String) f2.get(basicInfo);
            } catch (Exception ignore) {}
        }
        if (validFrom == null && request.getItemOverview() != null) {
            validFrom = request.getItemOverview().getValidFrom();
        }
        if (validTo == null && request.getItemOverview() != null) {
            validTo = request.getItemOverview().getValidTo();
        }
        if (StringUtils.hasText(validFrom)) {
            try {
                inquiry.setValidFromDate(webserver.util.DateUtil.parseDate(validFrom));
            } catch (Exception e) {
                inquiry.setValidFromDate(LocalDate.now());
            }
        }
        if (StringUtils.hasText(validTo)) {
            try {
                inquiry.setValidToDate(webserver.util.DateUtil.parseDate(validTo));
            } catch (Exception e) {
                inquiry.setValidToDate(LocalDate.now().plusMonths(6));
            }
        }

        // 支持概率和状态的更新
        if (basicInfo != null) {
            // 概率
            if (basicInfo.getNetValue() != null) {
                inquiry.setProbability(basicInfo.getNetValue().floatValue()); // 你可根据实际字段调整
            } else {
                inquiry.setProbability(95.0f);
            }
            // 状态
            if (StringUtils.hasText(basicInfo.getStatus())) {
                inquiry.setStatus(basicInfo.getStatus());
            } else {
                inquiry.setStatus("OPEN");
            }
        } else {
            inquiry.setProbability(95.0f);
            inquiry.setStatus("OPEN");
        }

        return inquiry;
    }
    
    /**
     * 插入询价单项目 - 使用完整的 ItemValidation 字段结构
     */
    private void insertInquiryItemsWithFullFields(Long inquiryId, InquiryEditRequest.ItemOverview itemOverview) {
        if (itemOverview == null || itemOverview.getItems() == null) {
            log.warn("insertInquiryItemsWithFullFields: itemOverview 或 items 为空");
            return;
        }
        
        String overviewReqDelivDate = itemOverview != null ? itemOverview.getReqDelivDate() : null;
        int insertIndex = 1;
        for (int i = 0; i < itemOverview.getItems().size(); i++) {
            InquiryEditRequest.InquiryItemDetail itemDetail = itemOverview.getItems().get(i);
            // material 为空则跳过
            if (!org.springframework.util.StringUtils.hasText(itemDetail.getMaterial())) {
                log.info("跳过 material 为空的项目: index={}, description={}", i + 1, itemDetail.getDescription());
                continue;
            }
            InquiryItem item = buildInquiryItemFromDetail(inquiryId, insertIndex, itemDetail, overviewReqDelivDate);
            log.info("插入项目 {}: material={}, description={}", insertIndex, item.getMaterialCode(), item.getDescription());
            int insertResult = inquiryMapper.insertInquiryItem(item);
            log.info("插入项目 {} 结果: {}", insertIndex, insertResult);
            insertIndex++;
        }
    }
    
    /**
     * 更新询价单项目 - 使用完整的 ItemValidation 字段结构
     */
    private void updateInquiryItemsWithFullFields(Long inquiryId, InquiryEditRequest.ItemOverview itemOverview) {
        log.info("智能更新询价单项目，inquiryId: {}", inquiryId);
        if (inquiryId == null) {
            log.error("inquiryId 为空，无法更新项目");
            throw new IllegalArgumentException("inquiryId 不能为空");
        }
        // 查询数据库现有项目
        List<InquiryItem> dbItems = inquiryMapper.findItemsByInquiryId(inquiryId);
        Map<Integer, InquiryItem> dbItemMap = new HashMap<>();
        for (InquiryItem dbItem : dbItems) {
            dbItemMap.put(dbItem.getItemNo(), dbItem);
        }
        // 前端传来的项目
        List<InquiryEditRequest.InquiryItemDetail> reqItems = (itemOverview != null && itemOverview.getItems() != null) ? itemOverview.getItems() : Collections.emptyList();
        Set<Integer> reqItemNos = new HashSet<>();
        int insertIndex = 1;
    String overviewReqDelivDate = itemOverview != null ? itemOverview.getReqDelivDate() : null;
    for (InquiryEditRequest.InquiryItemDetail itemDetail : reqItems) {
            // material 为空则跳过
            if (!org.springframework.util.StringUtils.hasText(itemDetail.getMaterial())) {
                log.info("跳过 material 为空的项目: index={}, description={}", insertIndex, itemDetail.getDescription());
                insertIndex++;
                continue;
            }
            Integer itemNo = null;
            try {
                itemNo = Integer.parseInt(itemDetail.getItem());
            } catch (Exception e) {
                itemNo = insertIndex;
            }
            reqItemNos.add(itemNo);
            InquiryItem item = buildInquiryItemFromDetail(inquiryId, itemNo, itemDetail, overviewReqDelivDate);
            if (dbItemMap.containsKey(itemNo)) {
                // 已存在，更新
                inquiryMapper.updateInquiryItem(item);
                log.info("更新项目 itemNo={}, reqDelivDate={}", itemNo, item.getReqDelivDate());
            } else {
                // 不存在，插入
                inquiryMapper.insertInquiryItem(item);
                log.info("插入新项目 itemNo={}, reqDelivDate={}", itemNo, item.getReqDelivDate());
            }
            insertIndex++;
        }
        // 删除数据库有但前端没传的项目
        for (InquiryItem dbItem : dbItems) {
            if (!reqItemNos.contains(dbItem.getItemNo())) {
                // 只删单个项目
                // 你可以实现 deleteInquiryItemByItemNo 方法
                log.info("需删除项目 itemNo={}", dbItem.getItemNo());
                // inquiryMapper.deleteInquiryItemByItemNo(inquiryId, dbItem.getItemNo());
            }
        }
    }
    
    /**
     * 从详细信息构建完整的询价单项目
     */
    private InquiryItem buildInquiryItemFromDetail(Long inquiryId, Integer itemNo, InquiryEditRequest.InquiryItemDetail itemDetail, String overviewReqDelivDate) {
        InquiryItem item = new InquiryItem();
        item.setInquiryId(inquiryId);
        item.setItemNo(itemNo);
        
        // 设置原有字段
        if (StringUtils.hasText(itemDetail.getMaterial())) {
            try {
                String matId = itemDetail.getMaterial().replace("MAT-", "");
                item.setMatId(Long.parseLong(matId));
            } catch (Exception e) {
                item.setMatId(1L);
            }
        } else {
            item.setMatId(1L);
        }
        
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
        
        item.setPlantId(1000L);
        item.setSu(StringUtils.hasText(itemDetail.getOrderQuantityUnit()) ? 
                  itemDetail.getOrderQuantityUnit() : "EA");
        
        // 设置新增的完整字段
        item.setItemCode(itemNo.toString());
        item.setMaterialCode(itemDetail.getMaterial());
        item.setOrderQuantityStr(itemDetail.getOrderQuantity());
        item.setOrderQuantityUnit(itemDetail.getOrderQuantityUnit());
        item.setDescription(itemDetail.getDescription());
        // 优先用 itemDetail 的 reqDelivDate，如果没有则兜底用 overview 的 reqDelivDate，不再兜底为当前日期
        String reqDelivDate = itemDetail.getReqDelivDate();
        if (!org.springframework.util.StringUtils.hasText(reqDelivDate)) {
            reqDelivDate = overviewReqDelivDate;
        }
        item.setReqDelivDate(reqDelivDate);
        item.setNetValueStr(itemDetail.getNetValue());
        item.setNetValueUnit(itemDetail.getNetValueUnit());
        item.setTaxValueStr(itemDetail.getTaxValue());
        item.setTaxValueUnit(itemDetail.getTaxValueUnit());
        item.setPricingDate(itemDetail.getPricingDate());
        item.setOrderProbability(itemDetail.getOrderProbability());
        
        // 处理定价元素
        if (itemDetail.getPricingElements() != null && !itemDetail.getPricingElements().isEmpty()) {
            try {
                item.setPricingElementsJson(objectMapper.writeValueAsString(itemDetail.getPricingElements()));
            } catch (Exception e) {
                log.warn("序列化定价元素失败: {}", e.getMessage());
                item.setPricingElementsJson("[]");
            }
        } else {
            item.setPricingElementsJson("[]");
        }
        
        return item;
    }
    
    /**
     * 转换为 ItemValidationRequest 格式
     */
    private List<ItemValidationRequest> convertToItemValidationRequests(List<InquiryItemsTabQueryRequest.ItemQuery> items) {
        List<ItemValidationRequest> requests = new ArrayList<>();
        
        for (InquiryItemsTabQueryRequest.ItemQuery queryItem : items) {
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
            request.setPricingElements(new ArrayList<>()); // 简化处理
            
            requests.add(request);
        }
        
        return requests;
    }
    
    /**
     * 将验证结果转换为 Inquiry 的响应格式
     */
    private Map<String, Object> convertValidationResponseToInquiryFormat(ItemValidationResponse validationResponse) {
        Map<String, Object> data = new HashMap<>();
        if (validationResponse.getData() != null) {
            // result
            data.put("result", validationResponse.getData().getResult());

            // generalData
            data.put("generalData", validationResponse.getData().getGeneralData());

            // breakdowns
            List<Map<String, Object>> breakdowns = new ArrayList<>();
            if (validationResponse.getData().getBreakdowns() != null) {
                for (ItemValidationResponse.ItemBreakdown breakdown : validationResponse.getData().getBreakdowns()) {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("item", breakdown.getItem());
                    itemMap.put("material", breakdown.getMaterial());
                    itemMap.put("orderQuantity", breakdown.getOrderQuantity());
                    itemMap.put("orderQuantityUnit", breakdown.getOrderQuantityUnit());
                    itemMap.put("description", breakdown.getDescription());
                    itemMap.put("reqDelivDate", breakdown.getReqDelivDate());
                    itemMap.put("netValue", breakdown.getNetValue());
                    itemMap.put("netValueUnit", breakdown.getNetValueUnit());
                    itemMap.put("taxValue", breakdown.getTaxValue());
                    itemMap.put("taxValueUnit", breakdown.getTaxValueUnit());
                    itemMap.put("pricingDate", breakdown.getPricingDate());
                    itemMap.put("orderProbability", breakdown.getOrderProbability());
                    itemMap.put("pricingElements", breakdown.getPricingElements());
                    breakdowns.add(itemMap);
                }
            }
            data.put("breakdowns", breakdowns);
        }
        return data;
    }
    
    /**
     * 生成询价单编号
     */
    private String generateInquiryCode(Long inquiryId) {
        return String.format("INQ-2024-%03d", inquiryId);
    }
    
    /**
     * 解析询价单ID - 支持编号格式（INQ-2024-001）或直接数字
     */
    private Long parseInquiryId(String idStr) {
        try {
            // 如果是询价单编号格式，提取数字部分
            if (idStr.startsWith("INQ-")) {
                String numberPart = idStr.substring(idStr.lastIndexOf("-") + 1);
                return Long.parseLong(numberPart);
            } else {
                // 直接解析为数字
                return Long.parseLong(idStr);
            }
        } catch (Exception e) {
            log.warn("解析询价单ID失败: {}", idStr);
            return null;
        }
    }
}
