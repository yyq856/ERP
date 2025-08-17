package webserver.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import webserver.mapper.QuotationMapper;
import webserver.mapper.ItemMapper;
import webserver.pojo.*;
import webserver.common.Response;
import webserver.service.QuotationService;
import webserver.service.ValidateItemsService;
import webserver.service.UnifiedItemService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotationServiceImpl implements QuotationService {

    private final QuotationMapper quotationMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ValidateItemsService validateItemsService;

    @Autowired
    private UnifiedItemService unifiedItemService;

    @Override
    @Transactional
    public Response<QuotationResponseDTO1> createQuotationFromInquiry(CreateQuotationFromInquiryRequest request) {
        try {
            // 1. 获取 inquiry 信息
            var inquiry = quotationMapper.selectInquiryById(request.getInquiryId());
            var inquiryItems = quotationMapper.selectInquiryItemsById(request.getInquiryId());

            // 2. 插入 quotation 主表
            quotationMapper.insertQuotationFromInquiry(inquiry);

            // 获取新 quotation_id
            Long quotationId = quotationMapper.getLastInsertId();

            // 3. 插入 quotation item 表
            quotationMapper.insertQuotationItemsFromInquiry(quotationId, inquiryItems);

            // 4. 组装返回 DTO
            QuotationDetailsResponseDTO resp = buildResponseDTO(quotationId, inquiry, inquiryItems);
            QuotationResponseDTO1 response = new QuotationResponseDTO1();
            response.setQuotationData(resp);
            return Response.success(response);

        } catch (Exception e) {
            return Response.error("Quotation creation failed, please try again later.");
        }
    }

    private QuotationDetailsResponseDTO buildResponseDTO(Long quotationId, InquiryDTO inquiry, List<InquiryItemDTO> inquiryItems) {
        QuotationDetailsResponseDTO response = new QuotationDetailsResponseDTO();

        // meta
        QuotationDetailsResponseDTO.Meta meta = new QuotationDetailsResponseDTO.Meta();
        meta.setId(String.valueOf(quotationId));
        response.setMeta(meta);

        // basicInfo
        QuotationDetailsResponseDTO.BasicInfo basicInfo = new QuotationDetailsResponseDTO.BasicInfo();
        basicInfo.setQuotation(String.valueOf(quotationId));
        basicInfo.setSoldToParty(String.valueOf(inquiry.getSoldTp()));
        basicInfo.setShipToParty(String.valueOf(inquiry.getShipTp()));
        basicInfo.setCustomerReference(inquiry.getCustRef());
        basicInfo.setCustomerReferenceDate(inquiry.getCustomerReferenceDate() != null ?
            inquiry.getCustomerReferenceDate().toString() : "");
        basicInfo.setNetValue(inquiry.getNetValue());
        basicInfo.setNetValueUnit("USD"); // 可根据需求调整
        response.setBasicInfo(basicInfo);

        // itemOverview
        QuotationDetailsResponseDTO.ItemOverview itemOverview = new QuotationDetailsResponseDTO.ItemOverview();
        itemOverview.setValidFrom(inquiry.getValidFromDate() != null ?
            inquiry.getValidFromDate().toString() : "");
        itemOverview.setValidTo(inquiry.getValidToDate() != null ?
            inquiry.getValidToDate().toString() : "");
        itemOverview.setReqDelivDate(inquiry.getValidFromDate() != null ?
            inquiry.getValidFromDate().toString() : "");
        itemOverview.setExpectedOralVal("0");        // 示例，可根据需求改
        itemOverview.setExpectedOralValUnit("USD");  // 示例

        List<QuotationItemDTO> items = new ArrayList<>();
        for (InquiryItemDTO inItem : inquiryItems) {
            QuotationItemDTO dto = new QuotationItemDTO();

            // 基础字段
            dto.setItem(String.valueOf(inItem.getItemNo()));
            dto.setMaterial(String.valueOf(inItem.getMatId()));
            dto.setOrderQuantity(inItem.getOrderQuantityStr() != null ? inItem.getOrderQuantityStr() : String.valueOf(inItem.getQuantity()));
            dto.setOrderQuantityUnit(inItem.getOrderQuantityUnit() != null ? inItem.getOrderQuantityUnit() : inItem.getSu());
            dto.setDescription(inItem.getDescription() != null ? inItem.getDescription() : "");
            dto.setReqDelivDate(inItem.getReqDelivDate() != null ? inItem.getReqDelivDate() : "");
            dto.setNetValue(inItem.getNetValueStr() != null ? inItem.getNetValueStr() : String.valueOf(inItem.getItemValue()));
            dto.setNetValueUnit(inItem.getNetValueUnit() != null ? inItem.getNetValueUnit() : "CNY");
            dto.setTaxValue(inItem.getTaxValueStr() != null ? inItem.getTaxValueStr() : "");
            dto.setTaxValueUnit(inItem.getTaxValueUnit() != null ? inItem.getTaxValueUnit() : "CNY");
            dto.setPricingDate(inItem.getPricingDate() != null ? inItem.getPricingDate() : "");
            dto.setOrderProbability(inItem.getOrderProbability() != null ? inItem.getOrderProbability() : "100");

            // 解析定价元素
            dto.setPricingElements(parsePricingElements(inItem.getPricingElementsJson()));

            // 兼容性字段
            dto.setSu(inItem.getSu());
            dto.setAltItm(0);
            dto.setNetPrice(inItem.getNetPrice());

            items.add(dto);
        }
        itemOverview.setItems(items);

        response.setItemOverview(itemOverview);

        return response;
    }

    @Override
    public QuotationResponseDTO1 getQuotationDetails(String quotationId) {
        QuotationDTO quotation = quotationMapper.selectQuotationById(quotationId);
        if (quotation == null) {
            throw new RuntimeException("Quotation not found");
        }

        // ✅ 使用统一服务读取items并转换为前端格式
        List<Map<String, Object>> frontendItems = unifiedItemService.getDocumentItemsAsFrontendFormat(Long.parseLong(quotationId), "quotation");

        // ✅ 转换前端格式为QuotationItemDTO
        List<QuotationItemDTO> quotationItemDTOs = convertFrontendItemsToQuotationItemDTOs(frontendItems);

        QuotationDetailsResponseDTO details = buildResponseDTO1(quotation, quotationItemDTOs);

        // 外层包装
        QuotationResponseDTO1 response = new QuotationResponseDTO1();
        response.setQuotationData(details);

        return response;
    }

    private QuotationDetailsResponseDTO buildResponseDTO1(QuotationDTO quotation, List<QuotationItemDTO> items) {
        QuotationDetailsResponseDTO response = new QuotationDetailsResponseDTO();

        // meta
        QuotationDetailsResponseDTO.Meta meta = new QuotationDetailsResponseDTO.Meta();
        meta.setId(String.valueOf(quotation.getQuotationId()));
        response.setMeta(meta);

        // basicInfo
        QuotationDetailsResponseDTO.BasicInfo basicInfo = new QuotationDetailsResponseDTO.BasicInfo();
        if (quotation.getReferenceInquiryId() != null) {
            basicInfo.setInquiry(String.valueOf(quotation.getReferenceInquiryId()));
        }
        basicInfo.setQuotation(String.valueOf(quotation.getQuotationId()));
        basicInfo.setSoldToParty(String.valueOf(quotation.getSoldTp()));
        basicInfo.setShipToParty(String.valueOf(quotation.getShipTp()));
        basicInfo.setCustomerReference(quotation.getCustRef() != null ? quotation.getCustRef() : "");
        basicInfo.setNetValue(quotation.getNetValue() != null ? quotation.getNetValue() : 0);
        basicInfo.setNetValueUnit(quotation.getCurrency() != null ? quotation.getCurrency() : "USD");
        if (quotation.getCustomerReferenceDate() != null) {
            basicInfo.setCustomerReferenceDate(
                    new SimpleDateFormat("yyyy-MM-dd").format(quotation.getCustomerReferenceDate())
            );
        }
        response.setBasicInfo(basicInfo);

        // itemOverview
        QuotationDetailsResponseDTO.ItemOverview itemOverview = new QuotationDetailsResponseDTO.ItemOverview();
        if (quotation.getValidFromDate() != null) {
            itemOverview.setValidFrom(new SimpleDateFormat("yyyy-MM-dd").format(quotation.getValidFromDate()));
        }
        if (quotation.getValidToDate() != null) {
            itemOverview.setValidTo(new SimpleDateFormat("yyyy-MM-dd").format(quotation.getValidToDate()));
        }
        itemOverview.setReqDelivDate("");
        itemOverview.setExpectedOralVal("");
        itemOverview.setExpectedOralValUnit("");

        // ✅ 直接使用已经转换好的QuotationItemDTO列表
        // 统一服务已经处理了所有字段映射和JSON解析
        itemOverview.setItems(items);

        response.setItemOverview(itemOverview);

        return response;
    }

    /**
     * 将前端格式的items转换为QuotationItemDTO列表
     */
    private List<QuotationItemDTO> convertFrontendItemsToQuotationItemDTOs(List<Map<String, Object>> frontendItems) {
        List<QuotationItemDTO> dtos = new ArrayList<>();

        for (Map<String, Object> frontendItem : frontendItems) {
            QuotationItemDTO dto = new QuotationItemDTO();

            // 基础字段
            dto.setItem(getString(frontendItem, "item"));
            dto.setMaterial(getString(frontendItem, "material"));
            dto.setOrderQuantity(getString(frontendItem, "orderQuantity"));
            dto.setOrderQuantityUnit(getString(frontendItem, "orderQuantityUnit"));
            dto.setDescription(getString(frontendItem, "description"));
            dto.setSu(getString(frontendItem, "orderQuantityUnit", "0"));
            dto.setAltItm(0);

            // ItemValidation字段
            dto.setReqDelivDate(getString(frontendItem, "reqDelivDate"));
            dto.setNetValue(getString(frontendItem, "netValue"));
            dto.setNetValueUnit(getString(frontendItem, "netValueUnit"));
            dto.setTaxValue(getString(frontendItem, "taxValue"));
            dto.setTaxValueUnit(getString(frontendItem, "taxValueUnit"));
            dto.setPricingDate(getString(frontendItem, "pricingDate"));
            dto.setOrderProbability(getString(frontendItem, "orderProbability"));

            // pricingElements
            Object pricingElementsObj = frontendItem.get("pricingElements");
            if (pricingElementsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> pricingElementMaps = (List<Map<String, Object>>) pricingElementsObj;
                List<PricingElementDTO> pricingElements = new ArrayList<>();

                for (Map<String, Object> elementMap : pricingElementMaps) {
                    PricingElementDTO element = new PricingElementDTO();
                    element.setCnty(getString(elementMap, "cnty"));
                    element.setName(getString(elementMap, "name"));
                    element.setAmount(getString(elementMap, "amount"));
                    element.setCity(getString(elementMap, "city"));
                    element.setPer(getString(elementMap, "per"));
                    element.setUom(getString(elementMap, "uom"));
                    element.setConditionValue(getString(elementMap, "conditionValue"));
                    element.setCurr(getString(elementMap, "curr"));
                    element.setStatus(getString(elementMap, "status"));
                    element.setNumC(getString(elementMap, "numC"));
                    element.setAtoMtsComponent(getString(elementMap, "atoMtsComponent"));
                    element.setOun(getString(elementMap, "oun"));
                    element.setCconDe(getString(elementMap, "cconDe"));
                    element.setUn(getString(elementMap, "un"));
                    element.setConditionValue2(getString(elementMap, "conditionValue2"));
                    element.setCdCur(getString(elementMap, "cdCur"));

                    Object statObj = elementMap.get("stat");
                    element.setStat(statObj instanceof Boolean ? (Boolean) statObj : false);

                    pricingElements.add(element);
                }

                dto.setPricingElements(pricingElements);
            } else {
                dto.setPricingElements(new ArrayList<>());
            }

            dtos.add(dto);
        }

        return dtos;
    }

    /**
     * 安全地从Map中获取字符串值
     */
    private String getString(Map<String, Object> map, String key) {
        return getString(map, key, null);
    }

    /**
     * 安全地从Map中获取字符串值，带默认值
     */
    private String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }

    @Override
    public QuotationResponseDTO1 updateQuotation(QuotationResponseDTO1 request) {
        QuotationDetailsResponseDTO quotation = request.getQuotationData();

        if (quotation.getMeta() == null || quotation.getMeta().getId() == null) {
            throw new RuntimeException("Quotation ID cannot be null");
        }

        // 解析ID：如果是"QUO-2024-001"格式，提取最后一个"-"之后的部分
        String idStr = quotation.getMeta().getId();
        if (idStr.contains("-")) {
            idStr = idStr.substring(idStr.lastIndexOf("-") + 1);
        }
        Long quotationId = Long.parseLong(idStr);

        // 处理空字符串日期字段，转换为null
        if (quotation.getBasicInfo() != null) {
            if ("".equals(quotation.getBasicInfo().getCustomerReferenceDate())) {
                quotation.getBasicInfo().setCustomerReferenceDate(null);
            }

            // 处理soldToParty和shipToParty字段：如果是"CUST-12345"格式，提取数字部分
            String soldToParty = quotation.getBasicInfo().getSoldToParty();
            if (soldToParty != null && soldToParty.contains("-")) {
                soldToParty = soldToParty.substring(soldToParty.lastIndexOf("-") + 1);
                quotation.getBasicInfo().setSoldToParty(soldToParty);
            }

            String shipToParty = quotation.getBasicInfo().getShipToParty();
            if (shipToParty != null && shipToParty.contains("-")) {
                shipToParty = shipToParty.substring(shipToParty.lastIndexOf("-") + 1);
                quotation.getBasicInfo().setShipToParty(shipToParty);
            }

            // 注意：QuotationDetailsResponseDTO.BasicInfo中的netValue是float类型，不需要处理逗号
            // 如果需要处理带逗号的字符串，应该在JSON反序列化阶段处理
        }
        if (quotation.getItemOverview() != null) {
            if ("".equals(quotation.getItemOverview().getValidFrom())) {
                quotation.getItemOverview().setValidFrom(null);
            }
            if ("".equals(quotation.getItemOverview().getValidTo())) {
                quotation.getItemOverview().setValidTo(null);
            }
        }

        // 更新主表
        quotationMapper.updateQuotation(quotationId, quotation.getBasicInfo(), quotation.getItemOverview());

        // 更新明细 - 使用真正统一的方法
        List<QuotationItemDTO> items = quotation.getItemOverview().getItems();

        // 转换为统一的前端数据格式
        List<Map<String, Object>> frontendItems = convertQuotationItemsToFrontendFormat(items);

        // 调用统一服务
        unifiedItemService.updateDocumentItems(quotationId, "quotation", frontendItems);

        // 返回最新数据（仍然保持外层包装）
        QuotationResponseDTO1 response = new QuotationResponseDTO1();
        response.setQuotationData(quotation);

        return response;
    }

    /**
     * 将QuotationItemDTO转换为统一的前端数据格式
     */
    private List<Map<String, Object>> convertQuotationItemsToFrontendFormat(List<QuotationItemDTO> items) {
        List<Map<String, Object>> frontendItems = new ArrayList<>();

        for (QuotationItemDTO item : items) {
            Map<String, Object> frontendItem = new HashMap<>();

            // 基础字段
            frontendItem.put("item", item.getItem());
            frontendItem.put("material", item.getMaterial());
            frontendItem.put("orderQuantity", item.getOrderQuantity());
            frontendItem.put("orderQuantityUnit", item.getOrderQuantityUnit());
            frontendItem.put("description", item.getDescription());

            // ItemValidation字段
            frontendItem.put("reqDelivDate", item.getReqDelivDate());

            // 处理netValue：移除千分位分隔符
            String netValue = item.getNetValue();
            if (netValue != null) {
                netValue = netValue.replaceAll(",", "");
            }
            frontendItem.put("netValue", netValue);
            frontendItem.put("netValueUnit", item.getNetValueUnit());

            // 处理taxValue：移除千分位分隔符
            String taxValue = item.getTaxValue();
            if (taxValue != null) {
                taxValue = taxValue.replaceAll(",", "");
            }
            frontendItem.put("taxValue", taxValue);
            frontendItem.put("taxValueUnit", item.getTaxValueUnit());

            frontendItem.put("pricingDate", item.getPricingDate());
            frontendItem.put("orderProbability", item.getOrderProbability());
            frontendItem.put("pricingElements", item.getPricingElements());

            frontendItems.add(frontendItem);
        }

        return frontendItems;
    }


    @Override
    public QuotationSearchResponseDTO searchQuotations(QuotationSearchRequestDTO request) {
        List<Map<String, Object>> results = quotationMapper.searchQuotations(request);

        QuotationSearchResponseDTO response = new QuotationSearchResponseDTO();
        QuotationSearchResponseDTO.QuotationStruct struct = new QuotationSearchResponseDTO.QuotationStruct();

        List<QuotationSearchResponseDTO.QuotationNode> currentValue = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (Map<String, Object> row : results) {
            QuotationSearchResponseDTO.QuotationNode node = new QuotationSearchResponseDTO.QuotationNode();
            node.setSalesQuotation(String.valueOf(row.get("salesQuotation")));
            node.setSoldToParty(String.valueOf(row.get("soldToParty")));
            node.setCustomerReference(String.valueOf(row.get("customerReference")));
            node.setOverallStatus(String.valueOf(row.get("overallStatus")));
            Object exp = row.get("latestExpiration");
            node.setLatestExpiration(exp != null ? sdf.format(exp) : "");
            currentValue.add(node);
        }

        struct.setCurrentValue(currentValue);
        struct.setConfig(new Object());
        struct.setEditable(false);

        // children 配置固定
        List<QuotationSearchResponseDTO.QuotationChildNode> children = new ArrayList<>();
        children.add(createChildNode("number", "leaf", "salesQuotation", false, null));
        children.add(createChildNode("string", "leaf", "soldToParty", true, null));
        children.add(createChildNode("string", "leaf", "customerReference", false, null));
        children.add(createChildNode("selection", "leaf", "overallStatus", true,
                Map.of("options", new String[]{"New","Open","In Process","Completed"})));
        children.add(createChildNode("date", "leaf", "latestExpiration", false, null));
        struct.setChildren(children);

        response.setQuotationStruct(struct);
        return response;
    }

    private QuotationSearchResponseDTO.QuotationChildNode createChildNode(String varType, String nodeType, String name,
                                                                          boolean isEditable, Object config) {
        QuotationSearchResponseDTO.QuotationChildNode child = new QuotationSearchResponseDTO.QuotationChildNode();
        child.setVarType(varType);
        child.setNodeType(nodeType);
        child.setName(name);
        child.setEditable(isEditable);
        child.setConfig(config);
        return child;
    }

    // ========== 新增：支持完整ItemValidation字段的方法实现 ==========

    @Override
    public QuotationResponse initialize(QuotationInitializeRequest request) {
        try {
            // 参数验证
            if (!StringUtils.hasText(request.getQuotationType())) {
                return QuotationResponse.error("报价单类型不能为空");
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

            log.info("报价单初始化成功，类型: {}", request.getQuotationType());
            return QuotationResponse.success(data, "初始化报价单成功");

        } catch (Exception e) {
            log.error("报价单初始化异常: {}", e.getMessage(), e);
            return QuotationResponse.error("服务器内部错误");
        }
    }

    @Override
    public QuotationResponse get(QuotationGetRequest request) {
        try {
            // 参数验证
            if (!StringUtils.hasText(request.getQuotationId())) {
                return QuotationResponse.error("报价单ID不能为空");
            }

            // 解析报价单ID - 支持编号格式（QUO-2024-001）或直接数字
            Long quotationId = parseQuotationId(request.getQuotationId());
            if (quotationId == null) {
                return QuotationResponse.error("报价单ID格式不正确");
            }

            // 查询报价单
            QuotationDTO quotation = quotationMapper.selectQuotationById(quotationId.toString());
            if (quotation == null) {
                return QuotationResponse.error("报价单不存在");
            }

            // 查询报价单项目
            List<QuotationItemEntity> items = quotationMapper.findItemsByQuotationIdFromUnifiedTable(quotationId);

            // 构建响应数据 - 使用完整的 ItemValidation 字段结构
            Map<String, Object> content = buildQuotationContentWithFullFields(quotation, items);

            Map<String, Object> data = new HashMap<>();
            data.put("content", content);

            log.info("报价单查询成功，ID: {}", quotationId);
            return QuotationResponse.success(data, "获取报价单成功");

        } catch (Exception e) {
            log.error("报价单查询异常: {}", e.getMessage(), e);
            return QuotationResponse.error("服务器内部错误");
        }
    }

    @Override
    public QuotationResponse edit(QuotationEditRequest request) {
        try {
            // 简单实现，返回成功
            log.info("报价单编辑请求");
            return QuotationResponse.success(null, "编辑报价单成功");

        } catch (Exception e) {
            log.error("报价单编辑异常: {}", e.getMessage(), e);
            return QuotationResponse.error("服务器内部错误");
        }
    }

    @Override
    public QuotationResponse itemsTabQuery(List<QuotationItemsTabQueryRequest.ItemQuery> items) {
        try {
            if (items == null || items.isEmpty()) {
                return QuotationResponse.error("查询项目不能为空");
            }

            // 转换为 ItemValidationRequest 格式并调用验证服务（复用inquiry的逻辑）
            List<ItemValidationRequest> validationRequests = convertQuotationItemsToValidationRequests(items);
            ItemValidationResponse validationResponse = validateItemsService.validateItems(validationRequests);

            // 将验证结果转换为 Quotation 的响应格式
            Map<String, Object> data = convertValidationResponseToQuotationFormat(validationResponse);

            log.info("物品批量查询成功，查询项目数: {}", items.size());
            return QuotationResponse.success(data, "批量查询成功");

        } catch (Exception e) {
            log.error("物品批量查询异常: {}", e.getMessage(), e);
            return QuotationResponse.error("服务器内部错误");
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 解析报价单ID - 支持编号格式（QUO-2024-001）或直接数字
     */
    private Long parseQuotationId(String quotationId) {
        if (!StringUtils.hasText(quotationId)) {
            return null;
        }

        try {
            // 如果是纯数字，直接解析
            return Long.parseLong(quotationId);
        } catch (NumberFormatException e) {
            // 如果不是纯数字，尝试解析编号格式
            if (quotationId.startsWith("QUO-")) {
                String[] parts = quotationId.split("-");
                if (parts.length >= 3) {
                    try {
                        return Long.parseLong(parts[2]);
                    } catch (NumberFormatException ex) {
                        log.warn("无法解析报价单编号: {}", quotationId);
                        return null;
                    }
                }
            }
            log.warn("无法解析报价单ID: {}", quotationId);
            return null;
        }
    }

    /**
     * 构建报价单内容 - 使用完整字段
     */
    private Map<String, Object> buildQuotationContentWithFullFields(QuotationDTO quotation, List<QuotationItemEntity> items) {
        Map<String, Object> content = new HashMap<>();

        // 基本信息
        Map<String, Object> basicInfo = new HashMap<>();
        basicInfo.put("quotation", generateQuotationCode(quotation.getQuotationId()));
        basicInfo.put("soldToParty", quotation.getSoldTp());
        basicInfo.put("shipToParty", quotation.getShipTp());
        basicInfo.put("customerReference", quotation.getCustRef());
        basicInfo.put("netValue", quotation.getNetValue());
        basicInfo.put("netValueUnit", quotation.getCurrency());
        content.put("basicInfo", basicInfo);

        // 项目概览
        Map<String, Object> itemOverview = new HashMap<>();
        itemOverview.put("validFrom", quotation.getValidFromDate());
        itemOverview.put("validTo", quotation.getValidToDate());
        itemOverview.put("reqDelivDate", "");
        itemOverview.put("expectOralVal", quotation.getNetValue());
        itemOverview.put("expectOralValUnit", quotation.getCurrency());

        // 项目列表
        List<Map<String, Object>> itemList = new ArrayList<>();
        for (QuotationItemEntity item : items) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("item", item.getItemCode());
            itemMap.put("material", item.getMaterialCode());
            itemMap.put("orderQuantity", item.getOrderQuantityStr());
            itemMap.put("orderQuantityUnit", item.getOrderQuantityUnit());
            itemMap.put("description", item.getDescription());
            itemMap.put("reqDelivDate", item.getReqDelivDate());
            itemMap.put("netValue", item.getNetValueStr());
            itemMap.put("netValueUnit", item.getNetValueUnit());
            itemMap.put("taxValue", item.getTaxValueStr());
            itemMap.put("taxValueUnit", item.getTaxValueUnit());
            itemMap.put("pricingDate", item.getPricingDate());
            itemMap.put("orderProbability", item.getOrderProbability());
            itemMap.put("pricingElements", new ArrayList<>());
            itemList.add(itemMap);
        }
        itemOverview.put("items", itemList);
        content.put("itemOverview", itemOverview);

        return content;
    }

    /**
     * 解析定价元素JSON字符串
     */
    private List<PricingElementDTO> parsePricingElements(String pricingElementsJson) {
        if (pricingElementsJson == null || pricingElementsJson.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(pricingElementsJson, new TypeReference<List<PricingElementDTO>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse pricing elements JSON: {}", pricingElementsJson, e);
            return new ArrayList<>();
        }
    }

    /**
     * 转换 QuotationItemsTabQueryRequest.ItemQuery 到 ItemValidationRequest
     */
    private List<ItemValidationRequest> convertQuotationItemsToValidationRequests(List<QuotationItemsTabQueryRequest.ItemQuery> items) {
        List<ItemValidationRequest> validationRequests = new ArrayList<>();

        for (QuotationItemsTabQueryRequest.ItemQuery queryItem : items) {
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
     * 将验证结果转换为 Quotation 的响应格式
     */
    private Map<String, Object> convertValidationResponseToQuotationFormat(ItemValidationResponse validationResponse) {
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
     * 生成报价单编号
     */
    private String generateQuotationCode(Long quotationId) {
        return String.format("QUO-%d-%03d", LocalDate.now().getYear(), quotationId);
    }

    // 所有旧的item更新方法已删除，现在使用UnifiedItemService
}
