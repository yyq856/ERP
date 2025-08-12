package webserver.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import webserver.mapper.ValidateItemsMapper;
import webserver.pojo.ValidateItemsRequest;
import webserver.pojo.ValidateItemsResponse;
import webserver.service.ValidateItemsService;
import webserver.util.DateUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ValidateItemsServiceImpl implements ValidateItemsService {

    @Autowired
    private ValidateItemsMapper validateItemsMapper;

    @Override
    public ValidateItemsResponse validateAndCalculateItems(List<ValidateItemsRequest> request) {
        try {
            log.info("开始验证和计算物品信息，物品数量: {}", request != null ? request.size() : 0);
            
            if (request == null || request.isEmpty()) {
                return new ValidateItemsResponse(false, "请求数据不能为空", null);
            }

            // 验证数据合法性
            List<Integer> badRecordIndices = new ArrayList<>();
            List<ValidateItemsResponse.ItemBreakdown> validatedBreakdowns = new ArrayList<>();
            
            BigDecimal totalNetValue = BigDecimal.ZERO;
            BigDecimal totalTaxValue = BigDecimal.ZERO;
            
            for (int i = 0; i < request.size(); i++) {
                ValidateItemsRequest item = request.get(i);
                
                // 验证必填字段
                if (!isValidItem(item)) {
                    badRecordIndices.add(i);
                    log.warn("物品 {} 验证失败：缺少必填字段", i);
                    continue;
                }
                
                // 验证物料是否存在
                try {
                    Long materialId = Long.parseLong(item.getMaterial());
                    if (!validateItemsMapper.materialExists(materialId)) {
                        badRecordIndices.add(i);
                        log.warn("物品 {} 验证失败：物料 {} 不存在", i, item.getMaterial());
                        continue;
                    }
                } catch (NumberFormatException e) {
                    badRecordIndices.add(i);
                    log.warn("物品 {} 验证失败：物料ID {} 格式不正确", i, item.getMaterial());
                    continue;
                }
                
                // 计算定价信息
                ValidateItemsResponse.ItemBreakdown breakdown = calculateItemBreakdown(item);
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
            ValidateItemsResponse.ValidationResult result = new ValidateItemsResponse.ValidationResult();
            result.setAllDataLegal(badRecordIndices.isEmpty() ? 1 : 0);
            result.setBadRecordIndices(badRecordIndices);
            
            // 构建总体数据
            ValidateItemsResponse.GeneralData generalData = new ValidateItemsResponse.GeneralData();
            generalData.setNetValue(totalNetValue.toString());
            generalData.setNetValueUnit("CNY"); // 默认货币单位
            generalData.setExpectOralVal(totalTaxValue.add(totalNetValue).toString());
            generalData.setExpectOralValUnit("CNY");
            
            // 构建响应数据
            ValidateItemsResponse.ValidateItemsData responseData = new ValidateItemsResponse.ValidateItemsData();
            responseData.setResult(result);
            responseData.setGeneralData(generalData);
            responseData.setBreakdowns(validatedBreakdowns);
            
            log.info("物品验证完成，合法数据: {}, 不合法数据: {}", 
                    validatedBreakdowns.size(), badRecordIndices.size());
            
            return new ValidateItemsResponse(true, "批量验证成功", responseData);
            
        } catch (Exception e) {
            log.error("物品验证计算失败: {}", e.getMessage(), e);
            return new ValidateItemsResponse(false, "验证失败，请检查输入数据", null);
        }
    }
    
    /**
     * 验证物品基本信息是否合法
     */
    private boolean isValidItem(ValidateItemsRequest item) {
        return StringUtils.hasText(item.getItem()) &&
               StringUtils.hasText(item.getMaterial()) &&
               StringUtils.hasText(item.getOrderQuantity()) &&
               StringUtils.hasText(item.getOrderQuantityUnit());
    }
    
    /**
     * 计算物品的详细分解信息
     */
    private ValidateItemsResponse.ItemBreakdown calculateItemBreakdown(ValidateItemsRequest item) {
        ValidateItemsResponse.ItemBreakdown breakdown = new ValidateItemsResponse.ItemBreakdown();
        
        // 设置基本信息
        breakdown.setItem(item.getItem());
        breakdown.setMaterial(item.getMaterial());
        breakdown.setOrderQuantity(item.getOrderQuantity());
        breakdown.setOrderQuantityUnit(item.getOrderQuantityUnit());
        breakdown.setDescription(item.getDescription());
        breakdown.setReqDelivDate(item.getReqDelivDate());
        breakdown.setPricingDate(item.getPricingDate());
        breakdown.setOrderProbability(item.getOrderProbability());
        
        // 计算定价信息
        try {
            // 从数据库获取物料的标准价格
            Long materialId = Long.parseLong(item.getMaterial());
            BigDecimal standardPrice = validateItemsMapper.getMaterialStandardPrice(materialId);
            BigDecimal quantity = new BigDecimal(item.getOrderQuantity());
            
            // 计算净值
            BigDecimal netValue = standardPrice.multiply(quantity);
            breakdown.setNetValue(netValue.doubleValue());
            breakdown.setNetValueUnit(item.getNetValueUnit() != null ? item.getNetValueUnit() : "CNY");
            
            // 计算税值 (假设税率为10%)
            BigDecimal taxRate = new BigDecimal("0.10");
            BigDecimal taxValue = netValue.multiply(taxRate);
            breakdown.setTaxValue(taxValue.doubleValue());
            breakdown.setTaxValueUnit(item.getTaxValueUnit() != null ? item.getTaxValueUnit() : "CNY");
            
        } catch (Exception e) {
            log.warn("计算物品 {} 定价信息失败: {}", item.getItem(), e.getMessage());
            breakdown.setNetValue(0.0);
            breakdown.setTaxValue(0.0);
            breakdown.setNetValueUnit("CNY");
            breakdown.setTaxValueUnit("CNY");
        }
        
        // 处理定价元素
        if (item.getPricingElements() != null) {
            List<ValidateItemsResponse.PricingElementBreakdown> pricingElementBreakdowns = 
                item.getPricingElements().stream()
                    .map(this::convertPricingElement)
                    .collect(Collectors.toList());
            breakdown.setPricingElements(pricingElementBreakdowns);
        } else {
            breakdown.setPricingElements(new ArrayList<>());
        }
        
        return breakdown;
    }
    
    /**
     * 转换定价元素
     */
    private ValidateItemsResponse.PricingElementBreakdown convertPricingElement(
            ValidateItemsRequest.PricingElement element) {
        ValidateItemsResponse.PricingElementBreakdown breakdown = 
            new ValidateItemsResponse.PricingElementBreakdown();
        
        breakdown.setCnty(element.getCnty());
        breakdown.setName(element.getName());
        breakdown.setAmount(element.getAmount());
        breakdown.setCity(element.getCity());
        breakdown.setPer(element.getPer());
        breakdown.setUom(element.getUom());
        breakdown.setConditionValue(element.getConditionValue());
        breakdown.setCurr(element.getCurr());
        breakdown.setStatus(element.getStatus());
        breakdown.setNumC(element.getNumC());
        breakdown.setAtoMtsComponent(element.getAtoMtsComponent());
        breakdown.setOun(element.getOun());
        breakdown.setCconDe(element.getCconDe());
        breakdown.setUn(element.getUn());
        breakdown.setConditionValue2(element.getConditionValue2());
        breakdown.setCdCur(element.getCdCur());
        breakdown.setStat(element.getStat());
        
        return breakdown;
    }
}