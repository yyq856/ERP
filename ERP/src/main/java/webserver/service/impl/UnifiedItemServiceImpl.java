package webserver.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import webserver.mapper.ItemMapper;
import webserver.pojo.Item;
import webserver.pojo.QuotationItemDTO;
import webserver.pojo.PricingElementDTO;
import webserver.service.UnifiedItemService;
import webserver.service.SalesOrderCalculationService;
import webserver.event.SalesOrderItemsUpdatedEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.*;

/**
 * 统一的Item服务实现
 * 处理所有业务类型的item操作，统一使用erp_item表
 */
@Slf4j
@Service
public class UnifiedItemServiceImpl implements UnifiedItemService {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private SalesOrderCalculationService salesOrderCalculationService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void updateDocumentItems(Long documentId, String documentType, List<Map<String, Object>> frontendItems) {
        log.info("统一更新文档items，documentId: {}, documentType: {}, items数量: {}", 
            documentId, documentType, frontendItems != null ? frontendItems.size() : 0);
        
        // 1. 删除所有现有的items
        int deletedCount = itemMapper.deleteItemsByDocumentIdAndType(documentId, documentType);
        log.info("删除现有items数量: {}", deletedCount);
        
        // 2. 插入新的items，按顺序重新分配行号
        if (frontendItems != null && !frontendItems.isEmpty()) {
            int itemNo = 1; // 从1开始重新分配行号
            int insertedCount = 0;
            
            for (Map<String, Object> frontendItem : frontendItems) {
                // 检查material字段是否有效
                Object materialObj = frontendItem.get("material");
                if (materialObj != null && !materialObj.toString().trim().isEmpty()) {
                    try {
                        // 转换前端数据为统一的Item实体
                        Item item = convertFrontendItemToUnifiedItem(documentId, documentType, itemNo++, frontendItem);
                        
                        // 插入新的item
                        int result = itemMapper.insertItem(item);
                        if (result > 0) {
                            insertedCount++;
                            log.debug("插入item成功: itemNo={}, material={}, netValue={}, taxValue={}", 
                                item.getItemNo(), item.getMaterialCode(), item.getNetValueStr(), item.getTaxValueStr());
                        } else {
                            log.warn("插入item失败: itemNo={}, material={}", item.getItemNo(), item.getMaterialCode());
                        }
                    } catch (Exception e) {
                        log.error("处理item失败: {}, 错误: {}", frontendItem, e.getMessage(), e);
                    }
                } else {
                    log.debug("跳过material为空的item: {}", frontendItem);
                }
            }
            
            log.info("成功插入items数量: {}", insertedCount);
        }

        // 🔥 如果是销售订单，发布事件以在事务提交后触发金额重新计算
        if ("sales_order".equals(documentType)) {
            log.info("销售订单items更新完成，发布事件以触发金额重新计算，soId: {}", documentId);
            eventPublisher.publishEvent(new SalesOrderItemsUpdatedEvent(documentId));
        }

        log.info("统一更新文档items完成，documentId: {}, documentType: {}", documentId, documentType);
    }

    @Override
    @Transactional
    public void updateDocumentItemsWithoutEvent(Long documentId, String documentType, List<Map<String, Object>> frontendItems) {
        log.info("统一更新文档items（不触发事件），documentId: {}, documentType: {}, items数量: {}",
            documentId, documentType, frontendItems != null ? frontendItems.size() : 0);

        // 1. 删除所有现有的items
        int deletedCount = itemMapper.deleteItemsByDocumentIdAndType(documentId, documentType);
        log.info("删除现有items数量: {}", deletedCount);

        // 2. 插入新的items，按顺序重新分配行号
        if (frontendItems != null && !frontendItems.isEmpty()) {
            int itemNo = 1; // 从1开始重新分配行号
            int insertedCount = 0;

            for (Map<String, Object> frontendItem : frontendItems) {
                // 检查material字段是否有效
                Object materialObj = frontendItem.get("material");
                if (materialObj != null && !materialObj.toString().trim().isEmpty()) {
                    try {
                        // 转换前端数据为统一的Item实体
                        Item item = convertFrontendItemToUnifiedItem(documentId, documentType, itemNo++, frontendItem);

                        // 插入新的item
                        int result = itemMapper.insertItem(item);
                        if (result > 0) {
                            insertedCount++;
                            log.debug("插入item成功: itemNo={}, material={}, netValue={}, taxValue={}",
                                item.getItemNo(), item.getMaterialCode(), item.getNetValueStr(), item.getTaxValueStr());
                        } else {
                            log.warn("插入item失败: itemNo={}, material={}", item.getItemNo(), item.getMaterialCode());
                        }
                    } catch (Exception e) {
                        log.error("处理item失败: {}, 错误: {}", frontendItem, e.getMessage(), e);
                    }
                } else {
                    log.debug("跳过material为空的item: {}", frontendItem);
                }
            }

            log.info("成功插入items数量: {}", insertedCount);
        }

        // 🔥 注意：这个方法不发布事件，不会触发金额重新计算
        log.info("统一更新文档items完成（不触发事件），documentId: {}, documentType: {}", documentId, documentType);
    }

    /**
     * 监听销售订单明细更新事件，在事务提交后触发金额重新计算
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSalesOrderItemsUpdated(SalesOrderItemsUpdatedEvent event) {
        try {
            Long soId = event.getSalesOrderId();
            log.info("事务提交后处理销售订单明细更新事件，开始重新计算金额，soId: {}", soId);

            boolean success = salesOrderCalculationService.recalculateAndUpdateSalesOrderAmounts(soId);
            if (success) {
                log.info("销售订单 {} 金额自动重新计算成功", soId);
            } else {
                log.warn("销售订单 {} 金额自动重新计算失败", soId);
            }
        } catch (Exception e) {
            log.error("销售订单 {} 金额自动重新计算时出错: {}", event.getSalesOrderId(), e.getMessage(), e);
        }
    }

    @Override
    public List<Item> getDocumentItems(Long documentId, String documentType) {
        log.debug("查询文档items，documentId: {}, documentType: {}", documentId, documentType);
        return itemMapper.findItemsByDocumentIdAndType(documentId, documentType);
    }

    @Override
    public List<Map<String, Object>> getDocumentItemsAsFrontendFormat(Long documentId, String documentType) {
        log.info("统一读取文档items并转换为前端格式，documentId: {}, documentType: {}", documentId, documentType);

        // 1. 从统一表中读取items
        List<Item> items = itemMapper.findItemsByDocumentIdAndType(documentId, documentType);

        // 2. 转换为前端标准格式
        List<Map<String, Object>> frontendItems = new ArrayList<>();

        for (Item item : items) {
            Map<String, Object> frontendItem = convertItemToFrontendFormat(item);
            frontendItems.add(frontendItem);
        }

        log.info("成功转换items数量: {}", frontendItems.size());
        return frontendItems;
    }

    /**
     * 将统一的Item转换为前端标准格式
     */
    private Map<String, Object> convertItemToFrontendFormat(Item item) {
        Map<String, Object> frontendItem = new HashMap<>();

        // 基础字段
        frontendItem.put("item", item.getItemCode() != null ? item.getItemCode() : String.valueOf(item.getItemNo()));
        frontendItem.put("material", item.getMaterialCode() != null ? item.getMaterialCode() : String.valueOf(item.getMatId()));
        frontendItem.put("orderQuantity", item.getOrderQuantityStr() != null ? item.getOrderQuantityStr() : String.valueOf(item.getQuantity()));
        frontendItem.put("orderQuantityUnit", item.getOrderQuantityUnit() != null ? item.getOrderQuantityUnit() : item.getSu());
        frontendItem.put("description", item.getDescription() != null ? item.getDescription() : "");

        // ItemValidation字段
        frontendItem.put("reqDelivDate", item.getReqDelivDate());
        frontendItem.put("netValue", item.getNetValueStr());
        frontendItem.put("netValueUnit", item.getNetValueUnit());
        frontendItem.put("taxValue", item.getTaxValueStr());
        frontendItem.put("taxValueUnit", item.getTaxValueUnit());
        frontendItem.put("pricingDate", item.getPricingDate());
        frontendItem.put("orderProbability", item.getOrderProbability());

        // ✅ 解析pricingElements JSON
        if (item.getPricingElementsJson() != null && !item.getPricingElementsJson().isEmpty() && !"[]".equals(item.getPricingElementsJson())) {
            try {
                // 解析JSON字符串为List<Map<String, Object>>
                List<Map<String, Object>> pricingElements = objectMapper.readValue(
                    item.getPricingElementsJson(),
                    new TypeReference<List<Map<String, Object>>>() {}
                );
                frontendItem.put("pricingElements", pricingElements);
                log.debug("成功解析pricingElements: {}", pricingElements.size());
            } catch (Exception e) {
                log.warn("解析pricingElements JSON失败: {}, JSON: {}", e.getMessage(), item.getPricingElementsJson());
                frontendItem.put("pricingElements", new ArrayList<>());
            }
        } else {
            frontendItem.put("pricingElements", new ArrayList<>());
        }

        return frontendItem;
    }

    @Override
    @Transactional
    public void deleteDocumentItems(Long documentId, String documentType) {
        log.info("删除文档items，documentId: {}, documentType: {}", documentId, documentType);
        int deletedCount = itemMapper.deleteItemsByDocumentIdAndType(documentId, documentType);
        log.info("删除items数量: {}", deletedCount);
    }

    // 旧的convertItemsToDTO方法已删除，现在使用getDocumentItemsAsFrontendFormat统一方法

    /**
     * 将前端的item数据转换为统一的Item实体
     * 
     * @param documentId 文档ID
     * @param documentType 文档类型
     * @param itemNo 行号
     * @param frontendItem 前端item数据
     * @return Item实体
     */
    private Item convertFrontendItemToUnifiedItem(Long documentId, String documentType, int itemNo, Map<String, Object> frontendItem) {
        Item item = new Item();
        
        // 文档信息
        item.setDocumentId(documentId);
        item.setDocumentType(documentType);
        item.setItemNo(itemNo);
        
        // 基础字段 - 从前端数据中提取
        item.setMatId(Long.parseLong(getString(frontendItem, "material")));
        item.setQuantity(Integer.parseInt(getString(frontendItem, "orderQuantity")));
        item.setSu(getString(frontendItem, "orderQuantityUnit"));
        item.setDescription(getString(frontendItem, "description"));
        
        // 设置默认值
        item.setNetPrice(0.0f); // 可以根据需要从前端获取或计算
        item.setItemValue(0.0f); // 可以根据需要计算
        item.setPlantId(1000L); // 默认工厂
        
        // ✅ 完整的ItemValidation字段映射
        item.setItemCode(getString(frontendItem, "item", String.valueOf(itemNo)));
        item.setMaterialCode(getString(frontendItem, "material"));
        item.setOrderQuantityStr(getString(frontendItem, "orderQuantity"));
        item.setOrderQuantityUnit(getString(frontendItem, "orderQuantityUnit"));
        item.setReqDelivDate(getString(frontendItem, "reqDelivDate"));
        
        // ✅ 重要字段映射
        item.setNetValueStr(getString(frontendItem, "netValue"));
        item.setNetValueUnit(getString(frontendItem, "netValueUnit"));
        item.setTaxValueStr(getString(frontendItem, "taxValue"));
        item.setTaxValueUnit(getString(frontendItem, "taxValueUnit"));
        item.setPricingDate(getString(frontendItem, "pricingDate"));
        item.setOrderProbability(getString(frontendItem, "orderProbability"));
        
        // ✅ 处理定价元素JSON
        Object pricingElementsObj = frontendItem.get("pricingElements");
        if (pricingElementsObj != null) {
            try {
                if (pricingElementsObj instanceof List && !((List<?>) pricingElementsObj).isEmpty()) {
                    item.setPricingElementsJson(objectMapper.writeValueAsString(pricingElementsObj));
                } else {
                    item.setPricingElementsJson("[]");
                }
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
}
