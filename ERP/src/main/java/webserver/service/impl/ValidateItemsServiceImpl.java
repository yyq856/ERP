package webserver.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import webserver.mapper.ValidateItemsMapper;
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
        
        // 设置基本信息
        breakdown.setItem(String.valueOf(itemNumber)); // 行号-按顺序生成
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
            
            // 创建定价元素列表
            List<ItemValidationResponse.PricingElementBreakdown> pricingElements = new ArrayList<>();
            
            // 添加基础价格定价元素
            ItemValidationResponse.PricingElementBreakdown basePriceElement = createBasePricingElementForValidation(
                    standardPrice, quantity, breakdown.getOrderQuantityUnit());
            pricingElements.add(basePriceElement);
            
            // 处理用户输入的定价元素
            if (item.getPricingElements() != null && !item.getPricingElements().isEmpty()) {
                for (ItemValidationRequest.PricingElementRequest reqElement : item.getPricingElements()) {
                    ItemValidationResponse.PricingElementBreakdown element = convertPricingElementFromValidationRequest(
                            reqElement, breakdown.getOrderQuantityUnit(), standardPrice, quantity);
                    pricingElements.add(element);
                }
            }
            
            // 计算净值和税值
            BigDecimal netValue = calculateNetValueFromValidationPricingElements(pricingElements);
            breakdown.setNetValue(netValue.doubleValue());
            breakdown.setNetValueUnit("CNY");
            
            // 计算税值 (净值 * 13%)
            BigDecimal taxRate = new BigDecimal("0.13");
            BigDecimal taxValue = netValue.multiply(taxRate);
            breakdown.setTaxValue(taxValue.doubleValue());
            breakdown.setTaxValueUnit("CNY");
            
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
                generalData.setNetValue(response.getData().getGeneralData().getNetValue());
                generalData.setNetValueUnit(response.getData().getGeneralData().getNetValueUnit());
                generalData.setExpectOralVal(response.getData().getGeneralData().getExpectOralVal());
                generalData.setExpectOralValUnit(response.getData().getGeneralData().getExpectOralValUnit());
                convertedData.setGeneralData(generalData);
            }
            
            // 转换明细列表
            if (response.getData().getBreakdowns() != null) {
                List<ValidateItemsResponse.ItemBreakdown> breakdowns = response.getData().getBreakdowns().stream()
                    .map(this::convertToValidateItemsBreakdown)
                    .collect(Collectors.toList());
                convertedData.setBreakdowns(breakdowns);
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
}