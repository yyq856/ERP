package webserver.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import webserver.mapper.ValidateItemsMapper;
import webserver.mapper.PricingElementKeyMapper;
import webserver.entity.PricingElementKey;
import webserver.util.PricingRuleEngine;
import webserver.pojo.ItemValidationRequest;
import webserver.pojo.ItemValidationResponse;
import webserver.pojo.ItemsTabQueryRequest;
import webserver.pojo.ItemsTabQueryResponse;
import webserver.pojo.ValidateItemsRequest;
import webserver.pojo.ValidateItemsResponse;
import webserver.service.ValidateItemsService;
import webserver.util.DateUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ValidateItemsServiceImpl implements ValidateItemsService {

    @Autowired
    private ValidateItemsMapper validateItemsMapper;

    @Autowired
    private PricingElementKeyMapper pricingElementKeyMapper;

    @Override
    public ItemValidationResponse validateItems(List<ItemValidationRequest> request) {
        try {
            log.info("开始物品验证服务，物品数量: {}", request != null ? request.size() : 0);
            
            if (request == null || request.isEmpty()) {
                return new ItemValidationResponse(false, "请求数据不能为空", null);
            }

            // 验证数据合法性
            List<Integer> badRecordIndices = new ArrayList<>();
            List<ItemValidationResponse.ItemBreakdown> validatedBreakdowns = new ArrayList<>();
            
            BigDecimal totalNetValue = BigDecimal.ZERO;
            BigDecimal totalTaxValue = BigDecimal.ZERO;
            
            for (int i = 0; i < request.size(); i++) {
                ItemValidationRequest item = request.get(i);
                
                // 验证基本字段：只要有material字段就认为是有效的
                if (!isValidItemValidationRequest(item)) {
                    badRecordIndices.add(i);
                    log.warn("物品 {} 验证失败：material为空或无效", i);
                    continue;
                }
                
                // 计算定价信息
                ItemValidationResponse.ItemBreakdown breakdown = calculateItemValidationBreakdown(item, i + 1);
                validatedBreakdowns.add(breakdown);
                
                // 累加总值
                if (breakdown.getNetValue() != null) {
                    totalNetValue = totalNetValue.add(BigDecimal.valueOf(breakdown.getNetValue()));
                }
                if (breakdown.getTaxValue() != null) {
                    totalTaxValue = totalTaxValue.add(BigDecimal.valueOf(breakdown.getTaxValue()));
                }
            }
            
            // 构建验证结果
            ItemValidationResponse.ValidationResult result = new ItemValidationResponse.ValidationResult();
            result.setAllDataLegal(badRecordIndices.isEmpty() ? 1 : 0);
            result.setBadRecordIndices(badRecordIndices);
            
            // 构建总体数据
            ItemValidationResponse.GeneralData generalData = new ItemValidationResponse.GeneralData();
            generalData.setNetValue(totalNetValue.toString());
            generalData.setNetValueUnit("CNY"); // 默认货币单位
            
            // 计算预期口头值（比净值高10-15%，这里用13%）
            BigDecimal expectOralVal = totalNetValue.add(totalTaxValue);
            generalData.setExpectOralVal(expectOralVal.toString());
            generalData.setExpectOralValUnit("CNY");
            
            // 构建响应数据
            ItemValidationResponse.ItemValidationData responseData = new ItemValidationResponse.ItemValidationData();
            responseData.setResult(result);
            responseData.setGeneralData(generalData);
            responseData.setBreakdowns(validatedBreakdowns);
            
            log.info("物品验证服务完成，合法数据: {}, 不合法数据: {}",
                    validatedBreakdowns.size(), badRecordIndices.size());
            
            return new ItemValidationResponse(true, "批量验证成功", responseData);
            
        } catch (Exception e) {
            log.error("物品验证服务失败: {}", e.getMessage(), e);
            return new ItemValidationResponse(false, "验证失败，请检查输入数据", null);
        }
    }

    /**
     * 验证ItemValidationRequest基本信息是否合法
     */
    private boolean isValidItemValidationRequest(ItemValidationRequest item) {
        // 只要有物料号（非空字符串）就认为是有效的
        return StringUtils.hasText(item.getMaterial()) && !item.getMaterial().trim().isEmpty();
    }
    
    /**
     * 计算物品的详细分解信息（按照接口规范）
     */
    private ItemValidationResponse.ItemBreakdown calculateItemValidationBreakdown(
            ItemValidationRequest item, int itemNumber) {
        ItemValidationResponse.ItemBreakdown breakdown = new ItemValidationResponse.ItemBreakdown();
        
        // 设置基本信息 - Query操作保留用户输入的item号，只在缺失时自动补全
        breakdown.setItem(StringUtils.hasText(item.getItem()) ? item.getItem() : String.valueOf(itemNumber));
        breakdown.setMaterial(item.getMaterial());
        
        try {
            String materialDesc = "";
            String materialUnit = "EA";
            BigDecimal standardPrice = BigDecimal.ZERO;
            
            // 处理物料信息
            if (item.getMaterial().matches("\\d+")) {
                // 数字ID格式
                try {
                    Long materialId = Long.parseLong(item.getMaterial());
                    materialDesc = validateItemsMapper.getMaterialDescription(materialId);
                    materialUnit = validateItemsMapper.getMaterialBaseUnit(materialId);
                    standardPrice = validateItemsMapper.getMaterialStandardPrice(materialId);
                    
                    if (materialDesc == null) materialDesc = "";
                    if (materialUnit == null) materialUnit = "EA";
                    if (standardPrice == null) standardPrice = BigDecimal.ZERO;
                } catch (Exception e) {
                    log.warn("无法获取物料 {} 的信息，使用默认值: {}", item.getMaterial(), e.getMessage());
                }
            } else {
                // 非数字格式（如MAT-001），使用默认值
                materialDesc = "物料 " + item.getMaterial();
                standardPrice = new BigDecimal("100.00"); // 默认价格
            }
            
            // 设置物料相关信息 - 优先使用用户输入，否则使用默认值
            breakdown.setOrderQuantityUnit(StringUtils.hasText(item.getOrderQuantityUnit()) ? 
                                         item.getOrderQuantityUnit() : materialUnit);
            breakdown.setDescription(StringUtils.hasText(item.getDescription()) ? 
                                   item.getDescription() : materialDesc);
            
            // 设置日期信息 - 优先使用用户输入
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            breakdown.setReqDelivDate(StringUtils.hasText(item.getReqDelivDate()) ? 
                                    item.getReqDelivDate() : currentDate);
            breakdown.setPricingDate(StringUtils.hasText(item.getPricingDate()) ? 
                                   item.getPricingDate() : currentDate);
            breakdown.setOrderProbability(StringUtils.hasText(item.getOrderProbability()) ? 
                                        item.getOrderProbability() : "100");
            
            // 处理订单数量 - 优先使用用户输入
            BigDecimal quantity = BigDecimal.ONE; // 默认数量
            String userQuantity = "1"; // 默认显示值
            
            if (StringUtils.hasText(item.getOrderQuantity())) {
                try {
                    quantity = new BigDecimal(item.getOrderQuantity());
                    userQuantity = item.getOrderQuantity(); // 保持用户输入的原始值
                } catch (NumberFormatException e) {
                    log.warn("订单数量格式错误，使用默认值1: {}", item.getOrderQuantity());
                    quantity = BigDecimal.ONE;
                    userQuantity = "1";
                }
            }
            breakdown.setOrderQuantity(userQuantity);
            
            // 处理定价元素并计算价格
            List<ItemValidationResponse.PricingElementBreakdown> pricingElements =
                processPricingElementsWithNewLogic(item, breakdown, standardPrice, quantity);

            // 计算净值和税值
            BigDecimal unitNetValue = calculateNetValueFromValidationPricingElements(pricingElements);
            BigDecimal totalNetValue = unitNetValue.multiply(quantity);
            breakdown.setNetValue(totalNetValue.doubleValue());
            breakdown.setNetValueUnit(breakdown.getNetValueUnit() != null ? breakdown.getNetValueUnit() : "CNY");

            log.info("🔥 价格计算完成: 单价={}, 数量={}, 总净值={}", unitNetValue, quantity, totalNetValue);

            // 计算税值 (总净值 * 13%)
            BigDecimal taxRate = new BigDecimal("0.13");
            BigDecimal taxValue = totalNetValue.multiply(taxRate);
            breakdown.setTaxValue(taxValue.doubleValue());
            breakdown.setTaxValueUnit(breakdown.getNetValueUnit() != null ? breakdown.getNetValueUnit() : "CNY");
            
            breakdown.setPricingElements(pricingElements);
            
        } catch (Exception e) {
            log.warn("计算物品 {} 定价信息失败，使用默认值: {}", item.getItem(), e.getMessage());
            breakdown.setOrderQuantity("1");
            breakdown.setOrderQuantityUnit("EA");
            breakdown.setDescription("");
            breakdown.setReqDelivDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            breakdown.setPricingDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            breakdown.setOrderProbability("100");
            breakdown.setNetValue(0.0);
            breakdown.setTaxValue(0.0);
            breakdown.setNetValueUnit("CNY");
            breakdown.setTaxValueUnit("CNY");
            breakdown.setPricingElements(new ArrayList<>());
        }
        
        return breakdown;
    }
    
    /**
     * 创建基础价格定价元素（用于ItemValidation）
     */
    private ItemValidationResponse.PricingElementBreakdown createBasePricingElementForValidation(
            BigDecimal standardPrice, BigDecimal quantity, String unit) {
        ItemValidationResponse.PricingElementBreakdown element = new ItemValidationResponse.PricingElementBreakdown();
        
        element.setCnty("BASE");
        element.setName("基础价格");
        element.setAmount(standardPrice.toString());
        element.setCity("CNY");
        element.setPer("1");
        element.setUom(unit);
        
        // 计算条件值：基础价格 * 数量
        BigDecimal conditionValue = standardPrice.multiply(quantity);
        element.setConditionValue(conditionValue.toString());
        element.setCurr("CNY");
        
        // 设置默认值
        element.setStatus("");
        element.setNumC("");
        element.setAtoMtsComponent("");
        element.setOun("");
        element.setCconDe("");
        element.setUn("");
        element.setConditionValue2("");
        element.setCdCur("");
        element.setStat(true);
        
        return element;
    }
    
    /**
     * 从请求转换定价元素（用于ItemValidation）
     */
    private ItemValidationResponse.PricingElementBreakdown convertPricingElementFromValidationRequest(
            ItemValidationRequest.PricingElementRequest reqElement, String defaultUnit, 
            BigDecimal defaultPrice, BigDecimal quantity) {
        ItemValidationResponse.PricingElementBreakdown element = new ItemValidationResponse.PricingElementBreakdown();
        
        // 保持用户输入，空字段用默认值
        element.setCnty(StringUtils.hasText(reqElement.getCnty()) ? reqElement.getCnty() : "USER");
        element.setName(StringUtils.hasText(reqElement.getName()) ? reqElement.getName() : "用户定价元素");
        element.setAmount(StringUtils.hasText(reqElement.getAmount()) ? reqElement.getAmount() : defaultPrice.toString());
        element.setCity(StringUtils.hasText(reqElement.getCity()) ? reqElement.getCity() : "CNY");
        element.setPer(StringUtils.hasText(reqElement.getPer()) ? reqElement.getPer() : "1");
        element.setUom(StringUtils.hasText(reqElement.getUom()) ? reqElement.getUom() : defaultUnit);
        
        // 计算条件值 - 如果用户没有提供，根据amount和quantity计算
        if (StringUtils.hasText(reqElement.getConditionValue())) {
            element.setConditionValue(reqElement.getConditionValue());
        } else {
            try {
                BigDecimal amount = StringUtils.hasText(reqElement.getAmount()) ? 
                                  new BigDecimal(reqElement.getAmount()) : defaultPrice;
                BigDecimal condValue = amount.multiply(quantity);
                element.setConditionValue(condValue.toString());
            } catch (NumberFormatException e) {
                element.setConditionValue("0");
            }
        }
        
        element.setCurr(StringUtils.hasText(reqElement.getCurr()) ? reqElement.getCurr() : "CNY");
        element.setStatus(StringUtils.hasText(reqElement.getStatus()) ? reqElement.getStatus() : "");
        element.setNumC(StringUtils.hasText(reqElement.getNumC()) ? reqElement.getNumC() : "");
        element.setAtoMtsComponent(StringUtils.hasText(reqElement.getAtoMtsComponent()) ? reqElement.getAtoMtsComponent() : "");
        element.setOun(StringUtils.hasText(reqElement.getOun()) ? reqElement.getOun() : "");
        element.setCconDe(StringUtils.hasText(reqElement.getCconDe()) ? reqElement.getCconDe() : "");
        element.setUn(StringUtils.hasText(reqElement.getUn()) ? reqElement.getUn() : "");
        element.setConditionValue2(StringUtils.hasText(reqElement.getConditionValue2()) ? reqElement.getConditionValue2() : "");
        element.setCdCur(StringUtils.hasText(reqElement.getCdCur()) ? reqElement.getCdCur() : "");
        element.setStat(reqElement.getStat() != null ? reqElement.getStat() : true);
        
        return element;
    }
    
    /**
     * 从定价元素列表计算净值（用于ItemValidation）
     */
    private BigDecimal calculateNetValueFromValidationPricingElements(List<ItemValidationResponse.PricingElementBreakdown> elements) {
        BigDecimal netValue = BigDecimal.ZERO;
        
        for (ItemValidationResponse.PricingElementBreakdown element : elements) {
            if (StringUtils.hasText(element.getConditionValue())) {
                try {
                    BigDecimal conditionValue = new BigDecimal(element.getConditionValue());
                    netValue = netValue.add(conditionValue);
                } catch (NumberFormatException e) {
                    log.warn("定价元素条件值格式错误: {}", element.getConditionValue());
                }
            }
        }
        
        return netValue;
    }

    // 保留原有方法以保持兼容性 - 转发到新方法
    @Override
    public ValidateItemsResponse validateAndCalculateItems(List<ValidateItemsRequest> request) {
        try {
            // 转换请求格式并调用新方法
            List<ItemValidationRequest> convertedRequest = convertValidateItemsRequest(request);
            ItemValidationResponse newResponse = validateItems(convertedRequest);
            
            // 转换响应格式
            return convertToValidateItemsResponse(newResponse);
        } catch (Exception e) {
            log.error("调用废弃方法 validateAndCalculateItems 失败: {}", e.getMessage(), e);
            return new ValidateItemsResponse(false, "验证失败，请检查输入数据", null);
        }
    }
    
    @Override
    public ItemsTabQueryResponse processItemsTabQuery(List<ItemsTabQueryRequest> request, String applicationType) {
        try {
            // 转换请求格式并调用新方法
            List<ItemValidationRequest> convertedRequest = convertItemsTabQueryRequest(request);
            ItemValidationResponse newResponse = validateItems(convertedRequest);
            
            // 转换响应格式
            return convertToItemsTabQueryResponse(newResponse);
        } catch (Exception e) {
            log.error("调用废弃方法 processItemsTabQuery 失败: {}", e.getMessage(), e);
            return new ItemsTabQueryResponse(false, "验证失败，请检查输入数据", null);
        }
    }
    
    /**
     * 转换 ValidateItemsRequest 到 ItemValidationRequest
     */
    private List<ItemValidationRequest> convertValidateItemsRequest(List<ValidateItemsRequest> request) {
        if (request == null) return new ArrayList<>();
        
        return request.stream().map(item -> {
            ItemValidationRequest converted = new ItemValidationRequest();
            converted.setItem(item.getItem() != null ? item.getItem() : "");
            converted.setMaterial(item.getMaterial() != null ? item.getMaterial() : "");
            converted.setOrderQuantity(item.getOrderQuantity() != null ? item.getOrderQuantity() : "");
            converted.setOrderQuantityUnit(item.getOrderQuantityUnit() != null ? item.getOrderQuantityUnit() : "");
            converted.setDescription(item.getDescription() != null ? item.getDescription() : "");
            converted.setReqDelivDate(item.getReqDelivDate() != null ? item.getReqDelivDate() : "");
            converted.setNetValue(item.getNetValue() != null ? item.getNetValue() : "");
            converted.setNetValueUnit(item.getNetValueUnit() != null ? item.getNetValueUnit() : "");
            converted.setTaxValue(item.getTaxValue() != null ? item.getTaxValue() : "");
            converted.setTaxValueUnit(item.getTaxValueUnit() != null ? item.getTaxValueUnit() : "");
            converted.setPricingDate(item.getPricingDate() != null ? item.getPricingDate() : "");
            converted.setOrderProbability(item.getOrderProbability() != null ? item.getOrderProbability() : "");
            converted.setPricingElements(new ArrayList<>()); // 简化处理
            return converted;
        }).collect(Collectors.toList());
    }
    
    /**
     * 转换 ItemsTabQueryRequest 到 ItemValidationRequest
     */
    private List<ItemValidationRequest> convertItemsTabQueryRequest(List<ItemsTabQueryRequest> request) {
        if (request == null) return new ArrayList<>();
        
        return request.stream().map(item -> {
            ItemValidationRequest converted = new ItemValidationRequest();
            converted.setItem(item.getItem() != null ? item.getItem() : "");
            converted.setMaterial(item.getMaterial() != null ? item.getMaterial() : "");
            converted.setOrderQuantity(item.getOrderQuantity() != null ? item.getOrderQuantity() : "");
            converted.setOrderQuantityUnit(item.getOrderQuantityUnit() != null ? item.getOrderQuantityUnit() : "");
            converted.setDescription(item.getDescription() != null ? item.getDescription() : "");
            converted.setReqDelivDate(item.getReqDelivDate() != null ? item.getReqDelivDate() : "");
            converted.setNetValue(item.getNetValue() != null ? item.getNetValue() : "");
            converted.setNetValueUnit(item.getNetValueUnit() != null ? item.getNetValueUnit() : "");
            converted.setTaxValue(item.getTaxValue() != null ? item.getTaxValue() : "");
            converted.setTaxValueUnit(item.getTaxValueUnit() != null ? item.getTaxValueUnit() : "");
            converted.setPricingDate(item.getPricingDate() != null ? item.getPricingDate() : "");
            converted.setOrderProbability(item.getOrderProbability() != null ? item.getOrderProbability() : "");
            converted.setPricingElements(new ArrayList<>()); // 简化处理
            return converted;
        }).collect(Collectors.toList());
    }
    
    /**
     * 转换 ItemValidationResponse 到 ValidateItemsResponse
     */
    private ValidateItemsResponse convertToValidateItemsResponse(ItemValidationResponse response) {
        if (response == null) {
            return new ValidateItemsResponse(false, "转换失败", null);
        }
        
        // 转换数据结构
        ValidateItemsResponse.ValidateItemsData convertedData = null;
        if (response.getData() != null) {
            convertedData = new ValidateItemsResponse.ValidateItemsData();
            
            // 转换验证结果
            if (response.getData().getResult() != null) {
                ValidateItemsResponse.ValidationResult result = new ValidateItemsResponse.ValidationResult();
                result.setAllDataLegal(response.getData().getResult().getAllDataLegal());
                result.setBadRecordIndices(response.getData().getResult().getBadRecordIndices());
                convertedData.setResult(result);
            }
            
            // 转换总体数据
            if (response.getData().getGeneralData() != null) {
                ValidateItemsResponse.GeneralData generalData = new ValidateItemsResponse.GeneralData();
                // 对于非outbound delivery的情况，设置默认值
                generalData.setPickingStatus("N/A");
                generalData.setOverallStatus("N/A");
                generalData.setGiStatus("N/A");
                generalData.setReadyToPost(false);
                generalData.setGrossWeight("0.000");
                generalData.setGrossWeightUnit("KG");
                generalData.setNetWeight("0.000");
                generalData.setNetWeightUnit("KG");
                generalData.setVolume("0.000");
                generalData.setVolumeUnit("m³");
                convertedData.setGeneralData(generalData);
            }

            // 转换明细列表 - 这里需要转换为OutboundDeliveryItemDTO
            if (response.getData().getBreakdowns() != null) {
                // 对于非outbound delivery的情况，返回空列表
                convertedData.setBreakdowns(new ArrayList<>());
            }
        }
        
        return new ValidateItemsResponse(response.isSuccess(), response.getMessage(), convertedData);
    }
    
    /**
     * 转换 ItemValidationResponse 到 ItemsTabQueryResponse
     */
    private ItemsTabQueryResponse convertToItemsTabQueryResponse(ItemValidationResponse response) {
        if (response == null) {
            return new ItemsTabQueryResponse(false, "转换失败", null);
        }
        
        // 转换数据结构
        ItemsTabQueryResponse.ItemsTabQueryData convertedData = null;
        if (response.getData() != null) {
            convertedData = new ItemsTabQueryResponse.ItemsTabQueryData();
            
            // 转换验证结果
            if (response.getData().getResult() != null) {
                ItemsTabQueryResponse.ValidationResult result = new ItemsTabQueryResponse.ValidationResult();
                result.setAllDataLegal(response.getData().getResult().getAllDataLegal());
                result.setBadRecordIndices(response.getData().getResult().getBadRecordIndices());
                convertedData.setResult(result);
            }
            
            // 转换总体数据
            if (response.getData().getGeneralData() != null) {
                ItemsTabQueryResponse.GeneralData generalData = new ItemsTabQueryResponse.GeneralData();
                generalData.setNetValue(response.getData().getGeneralData().getNetValue());
                generalData.setNetValueUnit(response.getData().getGeneralData().getNetValueUnit());
                generalData.setExpectOralVal(response.getData().getGeneralData().getExpectOralVal());
                generalData.setExpectOralValUnit(response.getData().getGeneralData().getExpectOralValUnit());
                convertedData.setGeneralData(generalData);
            }
            
            // 转换明细列表
            if (response.getData().getBreakdowns() != null) {
                List<ItemsTabQueryResponse.ItemBreakdown> breakdowns = response.getData().getBreakdowns().stream()
                    .map(this::convertToItemsTabQueryBreakdown)
                    .collect(Collectors.toList());
                convertedData.setBreakdowns(breakdowns);
            }
        }
        
        return new ItemsTabQueryResponse(response.isSuccess(), response.getMessage(), convertedData);
    }
    
    /**
     * 转换明细数据到 ValidateItemsResponse.ItemBreakdown
     */
    private ValidateItemsResponse.ItemBreakdown convertToValidateItemsBreakdown(ItemValidationResponse.ItemBreakdown source) {
        ValidateItemsResponse.ItemBreakdown target = new ValidateItemsResponse.ItemBreakdown();
        target.setItem(source.getItem());
        target.setMaterial(source.getMaterial());
        target.setOrderQuantity(source.getOrderQuantity());
        target.setOrderQuantityUnit(source.getOrderQuantityUnit());
        target.setDescription(source.getDescription());
        target.setReqDelivDate(source.getReqDelivDate());
        target.setNetValue(source.getNetValue());
        target.setNetValueUnit(source.getNetValueUnit());
        target.setTaxValue(source.getTaxValue());
        target.setTaxValueUnit(source.getTaxValueUnit());
        target.setPricingDate(source.getPricingDate());
        target.setOrderProbability(source.getOrderProbability());
        
        // 转换定价元素
        if (source.getPricingElements() != null) {
            List<ValidateItemsResponse.PricingElementBreakdown> pricingElements = source.getPricingElements().stream()
                .map(this::convertToValidateItemsPricingElement)
                .collect(Collectors.toList());
            target.setPricingElements(pricingElements);
        }
        
        return target;
    }
    
    /**
     * 转换明细数据到 ItemsTabQueryResponse.ItemBreakdown
     */
    private ItemsTabQueryResponse.ItemBreakdown convertToItemsTabQueryBreakdown(ItemValidationResponse.ItemBreakdown source) {
        ItemsTabQueryResponse.ItemBreakdown target = new ItemsTabQueryResponse.ItemBreakdown();
        target.setItem(source.getItem());
        target.setMaterial(source.getMaterial());
        target.setOrderQuantity(source.getOrderQuantity());
        target.setOrderQuantityUnit(source.getOrderQuantityUnit());
        target.setDescription(source.getDescription());
        target.setReqDelivDate(source.getReqDelivDate());
        target.setNetValue(source.getNetValue());
        target.setNetValueUnit(source.getNetValueUnit());
        target.setTaxValue(source.getTaxValue());
        target.setTaxValueUnit(source.getTaxValueUnit());
        target.setPricingDate(source.getPricingDate());
        target.setOrderProbability(source.getOrderProbability());
        
        // 转换定价元素
        if (source.getPricingElements() != null) {
            List<ItemsTabQueryResponse.PricingElementBreakdown> pricingElements = source.getPricingElements().stream()
                .map(this::convertToItemsTabQueryPricingElement)
                .collect(Collectors.toList());
            target.setPricingElements(pricingElements);
        }
        
        return target;
    }
    
    /**
     * 转换定价元素到 ValidateItemsResponse.PricingElementBreakdown
     */
    private ValidateItemsResponse.PricingElementBreakdown convertToValidateItemsPricingElement(ItemValidationResponse.PricingElementBreakdown source) {
        ValidateItemsResponse.PricingElementBreakdown target = new ValidateItemsResponse.PricingElementBreakdown();
        target.setCnty(source.getCnty());
        target.setName(source.getName());
        target.setAmount(source.getAmount());
        target.setCity(source.getCity());
        target.setPer(source.getPer());
        target.setUom(source.getUom());
        target.setConditionValue(source.getConditionValue());
        target.setCurr(source.getCurr());
        target.setStatus(source.getStatus());
        target.setNumC(source.getNumC());
        target.setAtoMtsComponent(source.getAtoMtsComponent());
        target.setOun(source.getOun());
        target.setCconDe(source.getCconDe());
        target.setUn(source.getUn());
        target.setConditionValue2(source.getConditionValue2());
        target.setCdCur(source.getCdCur());
        target.setStat(source.getStat());
        return target;
    }
    
    /**
     * 转换定价元素到 ItemsTabQueryResponse.PricingElementBreakdown
     */
    private ItemsTabQueryResponse.PricingElementBreakdown convertToItemsTabQueryPricingElement(ItemValidationResponse.PricingElementBreakdown source) {
        ItemsTabQueryResponse.PricingElementBreakdown target = new ItemsTabQueryResponse.PricingElementBreakdown();
        target.setCnty(source.getCnty());
        target.setName(source.getName());
        target.setAmount(source.getAmount());
        target.setCity(source.getCity());
        target.setPer(source.getPer());
        target.setUom(source.getUom());
        target.setConditionValue(source.getConditionValue());
        target.setCurr(source.getCurr());
        target.setStatus(source.getStatus());
        target.setNumC(source.getNumC());
        target.setAtoMtsComponent(source.getAtoMtsComponent());
        target.setOun(source.getOun());
        target.setCconDe(source.getCconDe());
        target.setUn(source.getUn());
        target.setConditionValue2(source.getConditionValue2());
        target.setCdCur(source.getCdCur());
        target.setStat(source.getStat());
        return target;
    }

    /**
     * 按照新逻辑处理定价元素
     * @param item 物品验证请求
     * @param breakdown 物品明细
     * @param standardPrice 标准价格
     * @param quantity 数量
     * @return 处理后的定价元素列表
     */
    private List<ItemValidationResponse.PricingElementBreakdown> processPricingElementsWithNewLogic(
            ItemValidationRequest item, ItemValidationResponse.ItemBreakdown breakdown,
            BigDecimal standardPrice, BigDecimal quantity) {

        log.info("🔥 开始处理定价元素，用户传入元素数量: {}",
            item.getPricingElements() != null ? item.getPricingElements().size() : 0);

        try {
            // 设置默认值
            String netValueUnit = breakdown.getNetValueUnit() != null ? breakdown.getNetValueUnit() : "CNY";
            String orderQuantityUnit = breakdown.getOrderQuantityUnit() != null ? breakdown.getOrderQuantityUnit() : "EA";

            log.info("🔥 使用货币单位: {}, 数量单位: {}", netValueUnit, orderQuantityUnit);

            // 获取所有定价元素类型配置
            List<PricingElementKey> allPricingKeys = pricingElementKeyMapper.selectAll();
            log.info("🔥 加载定价元素配置: {} 个", allPricingKeys.size());

            // 处理用户输入的定价元素列表
            List<ItemValidationRequest.PricingElementRequest> userPricingElements =
                item.getPricingElements() != null ? item.getPricingElements() : new ArrayList<>();

            List<ItemValidationResponse.PricingElementBreakdown> validPricingElements = new ArrayList<>();
            List<ItemValidationRequest.PricingElementRequest> failedElements = new ArrayList<>();

            // 验证和补全用户输入的定价元素
            for (ItemValidationRequest.PricingElementRequest p : userPricingElements) {
                log.info("🔥 处理定价元素: cnty={}, amount={}, name={}, city={}",
                    p.getCnty(), p.getAmount(), p.getName(), p.getCity());

                // 检查是否为完全空行（除了status和stat之外的所有字段都为空）
                if (isCompletelyEmptyPricingElement(p)) {
                    log.info("🔥 跳过完全空的定价元素行");
                    continue;
                }

                // 如果cnty为空但其他字段不为空，放到失败列表中
                if (!StringUtils.hasText(p.getCnty())) {
                    log.warn("⚠️ 定价元素cnty为空但有其他数据，放入失败列表");
                    failedElements.add(p);
                    continue;
                }

                if (StringUtils.hasText(p.getStatus())) { // 说明是用户新传来的，需要补全数据
                    PricingElementKey keyConfig = findPricingElementKeyByName(allPricingKeys, p.getCnty());

                    if (keyConfig == null) {
                        log.warn("⚠️ 定价元素类型无效: {}", p.getCnty());
                        failedElements.add(p);
                        continue;
                    }

                    log.info("🔥 找到定价元素配置: {}", keyConfig.getDescription());

                    // 补全数据：name和city强制覆盖（后端优先级高），其他字段只在用户没有输入时补全
                    // name字段：强制使用后端配置的描述
                    p.setName(keyConfig.getDescription());

                    // city字段：强制使用后端配置的默认单位
                    p.setCity(keyConfig.getDefaultUnit() != null ? keyConfig.getDefaultUnit() : netValueUnit);

                    // 其他字段：只在用户没有输入时补全
                    if (!StringUtils.hasText(p.getPer())) {
                        p.setPer("1");
                    }
                    if (!StringUtils.hasText(p.getUom())) {
                        p.setUom(orderQuantityUnit);
                    }

                    // 强制设置货币（如果用户没有输入）
                    if (!StringUtils.hasText(p.getCurr())) {
                        p.setCurr(netValueUnit);
                    }

                    log.info("🔥 定价元素补全完成: cnty={}, name={}, amount={}, city={}",
                        p.getCnty(), p.getName(), p.getAmount(), p.getCity());

                    // 检查是否有amount来判断能否参与计算
                    if (!StringUtils.hasText(p.getAmount())) {
                        log.warn("⚠️ 定价元素金额为空，无法参与计算: {}", p.getCnty());
                        failedElements.add(p); // status保持原样，让用户继续输入
                        continue;
                    }
                }

                // 只有不在失败列表中的元素才能参与计算
                if (!failedElements.contains(p)) {
                    validPricingElements.add(convertToValidationPricingElement(p));
                    log.info("🔥 添加有效定价元素: {}", p.getCnty());
                } else {
                    log.warn("⚠️ 跳过失败的定价元素: {}", p.getCnty());
                }
            }

            // 如果没有BASE元素，自动添加
            boolean hasBase = validPricingElements.stream()
                .anyMatch(pe -> "BASE".equals(pe.getCnty()));

            if (!hasBase) {
                ItemValidationResponse.PricingElementBreakdown baseElement = createBasePricingElementForValidation(
                    standardPrice, quantity, orderQuantityUnit);
                validPricingElements.add(0, baseElement); // 添加到开头
            }

            // 按照 sort_key 排序
            validPricingElements.sort((a, b) -> {
                PricingElementKey keyA = findPricingElementKeyByName(allPricingKeys, a.getCnty());
                PricingElementKey keyB = findPricingElementKeyByName(allPricingKeys, b.getCnty());
                int sortA = keyA != null ? keyA.getSortKey() : 999;
                int sortB = keyB != null ? keyB.getSortKey() : 999;
                return Integer.compare(sortA, sortB);
            });

            // 执行价格计算逻辑
            BigDecimal amount = BigDecimal.ZERO;
            for (ItemValidationResponse.PricingElementBreakdown element : validPricingElements) {
                PricingElementKey keyConfig = findPricingElementKeyByName(allPricingKeys, element.getCnty());
                if (keyConfig != null) {
                    try {
                        BigDecimal elementAmount = new BigDecimal(element.getAmount());
                        BigDecimal per = new BigDecimal(element.getPer());

                        // 使用规则计算新金额
                        BigDecimal newAmount = PricingRuleEngine.useRule(keyConfig.getRule(), amount, elementAmount, per);
                        BigDecimal conditionValue = newAmount.subtract(amount);

                        // 保留两位小数
                        element.setConditionValue(conditionValue.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        amount = newAmount;
                        element.setStatus(""); // 标志着补全完成

                        log.info("🔥 定价元素计算: {} -> 条件值: {}, 累计金额: {}",
                            element.getCnty(), conditionValue, amount);
                    } catch (Exception e) {
                        log.error("❌ 定价元素计算失败: {}, 错误: {}", element.getCnty(), e.getMessage());
                        element.setConditionValue("0");
                    }
                } else {
                    element.setConditionValue("0");
                }
            }

            // 将失败的元素（保持原有status）添加到结果最后，按原来的相对顺序
            List<ItemValidationResponse.PricingElementBreakdown> allPricingElements = new ArrayList<>(validPricingElements);
            for (ItemValidationRequest.PricingElementRequest failedElement : failedElements) {
                ItemValidationResponse.PricingElementBreakdown failedBreakdown = convertToValidationPricingElement(failedElement);
                allPricingElements.add(failedBreakdown);
                log.info("🔥 添加失败元素到结果末尾: cnty={}, status={}", failedElement.getCnty(), failedElement.getStatus());
            }

            log.info("🔥 定价元素处理完成，有效元素: {}, 失败元素: {}, 最终金额: {}, 总返回元素: {}",
                validPricingElements.size(), failedElements.size(), amount, allPricingElements.size());

            return allPricingElements;

        } catch (Exception e) {
            log.error("❌ 处理定价元素时发生错误: {}", e.getMessage(), e);
            // 返回基础价格元素作为后备
            List<ItemValidationResponse.PricingElementBreakdown> fallbackElements = new ArrayList<>();
            fallbackElements.add(createBasePricingElementForValidation(standardPrice, quantity, "EA"));
            return fallbackElements;
        }
    }

    /**
     * 检查定价元素是否为完全空行（除了status和stat之外的所有字段都为空）
     */
    private boolean isCompletelyEmptyPricingElement(ItemValidationRequest.PricingElementRequest p) {
        return !StringUtils.hasText(p.getCnty()) &&
               !StringUtils.hasText(p.getName()) &&
               !StringUtils.hasText(p.getAmount()) &&
               !StringUtils.hasText(p.getCity()) &&
               !StringUtils.hasText(p.getPer()) &&
               !StringUtils.hasText(p.getUom()) &&
               !StringUtils.hasText(p.getConditionValue()) &&
               !StringUtils.hasText(p.getCurr()) &&
               !StringUtils.hasText(p.getNumC()) &&
               !StringUtils.hasText(p.getAtoMtsComponent()) &&
               !StringUtils.hasText(p.getOun()) &&
               !StringUtils.hasText(p.getCconDe()) &&
               !StringUtils.hasText(p.getUn()) &&
               !StringUtils.hasText(p.getConditionValue2()) &&
               !StringUtils.hasText(p.getCdCur());
               // 注意：不检查status和stat字段
    }

    /**
     * 根据名称查找定价元素配置
     */
    private PricingElementKey findPricingElementKeyByName(List<PricingElementKey> allKeys, String name) {
        if (name == null || allKeys == null) {
            return null;
        }
        return allKeys.stream()
            .filter(key -> name.equals(key.getName()))
            .findFirst()
            .orElse(null);
    }

    /**
     * 转换请求中的定价元素到响应格式
     */
    private ItemValidationResponse.PricingElementBreakdown convertToValidationPricingElement(
            ItemValidationRequest.PricingElementRequest request) {
        ItemValidationResponse.PricingElementBreakdown element = new ItemValidationResponse.PricingElementBreakdown();
        element.setCnty(request.getCnty());
        element.setName(request.getName());
        element.setAmount(request.getAmount());
        element.setCity(request.getCity());
        element.setPer(request.getPer());
        element.setUom(request.getUom());
        element.setConditionValue(request.getConditionValue());
        element.setCurr(request.getCurr());
        element.setStatus(request.getStatus());
        element.setNumC(request.getNumC());
        element.setAtoMtsComponent(request.getAtoMtsComponent());
        element.setOun(request.getOun());
        element.setCconDe(request.getCconDe());
        element.setConditionValue2(request.getConditionValue2());
        element.setCdCur(request.getCdCur());
        element.setStat(request.getStat());
        return element;
    }
}