package webserver.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import webserver.dto.ItemCalculationResult;
import webserver.service.ItemCalculationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * 物品计算服务实现
 * 计算逻辑与 UnifiedItemServiceImpl 中的存储逻辑保持完全一致
 */
@Slf4j
@Service
public class ItemCalculationServiceImpl implements ItemCalculationService {
    
    @Override
    public ItemCalculationResult calculateItemsValue(List<Map<String, Object>> frontendItems) {
        log.info("开始计算物品列表总价格，物品数量: {}", frontendItems != null ? frontendItems.size() : 0);
        
        if (frontendItems == null || frontendItems.isEmpty()) {
            log.info("物品列表为空，返回零值");
            return new ItemCalculationResult("0", "USD", "0", "USD");
        }
        
        BigDecimal totalNetValue = BigDecimal.ZERO;
        BigDecimal totalTaxValue = BigDecimal.ZERO;
        String currency = "USD"; // 默认货币
        
        int validItemCount = 0;
        
        for (Map<String, Object> frontendItem : frontendItems) {
            // 检查material字段是否有效（与UnifiedItemServiceImpl逻辑一致）
            Object materialObj = frontendItem.get("material");
            if (materialObj != null && !materialObj.toString().trim().isEmpty()) {
                try {
                    // 获取数量
                    int quantity = getIntValue(frontendItem, "orderQuantity", 1);
                    
                    // 获取净值和税值（字符串格式）
                    String netValueStr = getStringValue(frontendItem, "netValue", "0");
                    String taxValueStr = getStringValue(frontendItem, "taxValue", "0");
                    
                    // 获取货币单位
                    String netValueUnit = getStringValue(frontendItem, "netValueUnit", "USD");
                    if (validItemCount == 0) {
                        currency = netValueUnit; // 使用第一个有效物品的货币作为总货币
                    }
                    
                    // 解析净值和税值（移除千分位分隔符）
                    BigDecimal netValue = parseDecimalValue(netValueStr);
                    BigDecimal taxValue = parseDecimalValue(taxValueStr);
                    
                    // 直接使用物品的净值和税值（已经在ValidateItemsServiceImpl中乘过数量了）
                    BigDecimal itemNetValue = netValue;
                    BigDecimal itemTaxValue = taxValue;
                    
                    // 累加到总值
                    totalNetValue = totalNetValue.add(itemNetValue);
                    totalTaxValue = totalTaxValue.add(itemTaxValue);
                    
                    validItemCount++;
                    
                    log.debug("处理物品: material={}, quantity={}, netValue={}, taxValue={}, itemNetValue={}, itemTaxValue={}", 
                        materialObj, quantity, netValue, taxValue, itemNetValue, itemTaxValue);
                        
                } catch (Exception e) {
                    log.error("处理物品时出错: {}, 错误: {}", frontendItem, e.getMessage(), e);
                }
            } else {
                log.debug("跳过material为空的物品: {}", frontendItem);
            }
        }
        
        // 格式化结果（保留2位小数，添加千分位分隔符）
        String formattedNetValue = formatDecimalValue(totalNetValue);
        String formattedTaxValue = formatDecimalValue(totalTaxValue);
        
        log.info("物品列表总价格计算完成，有效物品数: {}, 总净值: {} {}, 总税值: {} {}", 
            validItemCount, formattedNetValue, currency, formattedTaxValue, currency);
        
        return new ItemCalculationResult(formattedNetValue, currency, formattedTaxValue, currency);
    }
    
    /**
     * 获取整数值
     */
    private int getIntValue(Map<String, Object> item, String key, int defaultValue) {
        Object value = item.get(key);
        if (value == null) return defaultValue;
        
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.warn("无法解析整数值: key={}, value={}, 使用默认值: {}", key, value, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * 获取字符串值
     */
    private String getStringValue(Map<String, Object> item, String key, String defaultValue) {
        Object value = item.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    /**
     * 解析十进制数值（移除千分位分隔符）
     */
    private BigDecimal parseDecimalValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        try {
            // 移除千分位分隔符
            String cleanValue = value.replace(",", "");
            return new BigDecimal(cleanValue);
        } catch (NumberFormatException e) {
            log.warn("无法解析十进制数值: {}, 使用0", value);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 格式化十进制数值（添加千分位分隔符，保留2位小数）
     */
    private String formatDecimalValue(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        
        // 保留2位小数
        BigDecimal rounded = value.setScale(2, RoundingMode.HALF_UP);
        
        // 添加千分位分隔符
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        return formatter.format(rounded);
    }
}
