package webserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import webserver.common.Response;
import webserver.pojo.Item;
import webserver.service.UnifiedItemService;

import java.util.List;
import java.util.Map;

/**
 * 统一Item测试控制器
 * 用于测试统一的item操作
 */
@Slf4j
@RestController
@RequestMapping("/api/test/unified-item")
@CrossOrigin(origins = "*")
public class UnifiedItemTestController {

    @Autowired
    private UnifiedItemService unifiedItemService;

    /**
     * 测试更新文档items
     */
    @PostMapping("/update")
    public Response<?> testUpdateItems(@RequestBody Map<String, Object> request) {
        try {
            log.info("测试统一更新items");
            
            // 提取参数
            Long documentId = Long.parseLong(request.get("documentId").toString());
            String documentType = request.get("documentType").toString();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");
            
            // 调用统一服务
            unifiedItemService.updateDocumentItems(documentId, documentType, items);
            
            return Response.success("统一更新items成功");
            
        } catch (Exception e) {
            log.error("测试统一更新items失败: {}", e.getMessage(), e);
            return Response.error("测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试查询文档items
     */
    @GetMapping("/get")
    public Response<List<Item>> testGetItems(@RequestParam Long documentId, @RequestParam String documentType) {
        try {
            log.info("测试查询文档items，documentId: {}, documentType: {}", documentId, documentType);
            
            List<Item> items = unifiedItemService.getDocumentItems(documentId, documentType);
            
            return Response.success(items);
            
        } catch (Exception e) {
            log.error("测试查询items失败: {}", e.getMessage(), e);
            return Response.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 测试删除文档items
     */
    @DeleteMapping("/delete")
    public Response<?> testDeleteItems(@RequestParam Long documentId, @RequestParam String documentType) {
        try {
            log.info("测试删除文档items，documentId: {}, documentType: {}", documentId, documentType);
            
            unifiedItemService.deleteDocumentItems(documentId, documentType);
            
            return Response.success("删除成功");
            
        } catch (Exception e) {
            log.error("测试删除items失败: {}", e.getMessage(), e);
            return Response.error("删除失败: " + e.getMessage());
        }
    }
}
