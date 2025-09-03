package webserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物品计算结果 DTO
 * 用于返回物品列表的总价格计算结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCalculationResult {
    
    /**
     * 净值（字符串格式，包含千分位分隔符）
     */
    private String netValue;
    
    /**
     * 净值货币单位
     */
    private String netValueUnit;
    
    /**
     * 税值（字符串格式，包含千分位分隔符）
     */
    private String taxValue;
    
    /**
     * 税值货币单位
     */
    private String taxValueUnit;
}
