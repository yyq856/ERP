package webserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import webserver.dto.ItemCalculationResult;
import webserver.common.Response;
import webserver.service.ItemCalculationService;

import java.util.List;
import java.util.Map;

/**
 * 物品相关接口控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/item")
@CrossOrigin(origins = "*")
public class ItemController {
    
    @Autowired
    private ItemCalculationService itemCalculationService;
    
    /**
     * 计算物品列表的总价格
     * 
     * @param frontendItems 前端传入的物品列表
     * @return 计算结果，包含净值、税值及其货币单位
     */
    @PostMapping("/cal-value")
    public Response<ItemCalculationResult> calculateItemsValue(@RequestBody List<Map<String, Object>> frontendItems) {
        log.info("收到物品价格计算请求，物品数量: {}", frontendItems != null ? frontendItems.size() : 0);
        
        try {
            ItemCalculationResult result = itemCalculationService.calculateItemsValue(frontendItems);
            log.info("物品价格计算成功: {}", result);
            return Response.success(result);
        } catch (Exception e) {
            log.error("物品价格计算失败: {}", e.getMessage(), e);
            return Response.error("物品价格计算失败: " + e.getMessage());
        }
    }
}
