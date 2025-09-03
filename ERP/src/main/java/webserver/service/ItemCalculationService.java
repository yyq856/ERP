package webserver.service;

import webserver.dto.ItemCalculationResult;
import java.util.List;
import java.util.Map;

/**
 * 物品计算服务接口
 * 用于计算物品列表的总价格，不涉及数据库操作
 */
public interface ItemCalculationService {
    
    /**
     * 计算物品列表的总价格
     * 
     * @param frontendItems 前端传入的物品列表，格式与统一物品写入方法的输入相同
     * @return 计算结果，包含净值、税值及其货币单位
     */
    ItemCalculationResult calculateItemsValue(List<Map<String, Object>> frontendItems);
}
