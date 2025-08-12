package webserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import webserver.pojo.MaterialDocumentDetailResponse;
import webserver.pojo.MaterialDocumentSearchRequest;
import webserver.pojo.MaterialDocumentSearchResponse;
import webserver.service.MaterialDocumentService;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class MaterialDocumentController {

    @Autowired
    private MaterialDocumentService materialDocumentService;

    /**
     * 搜索物料凭证
     * 接口地址：POST /api/material/search
     * @param request 搜索条件
     * @return 搜索结果
     */
    @PostMapping("/api/material/search")
    public MaterialDocumentSearchResponse searchMaterialDocuments(@RequestBody MaterialDocumentSearchRequest request) {
        log.info("物料凭证搜索请求: {}", request);
        try {
            MaterialDocumentSearchResponse response = materialDocumentService.searchMaterialDocuments(request);
            log.info("物料凭证搜索完成，结果数量: {}",
                    response.getData() != null ? response.getData().size() : 0);
            return response;
        } catch (Exception e) {
            log.error("物料凭证搜索异常: {}", e.getMessage(), e);
            return new MaterialDocumentSearchResponse(false, "搜索失败: " + e.getMessage(), null);
        }
    }

    /**
     * 根据ID查询物料凭证详情
     * 接口地址：GET /api/material/get/{materialDocumentId}
     * @param materialDocumentId 物料凭证ID
     * @return 物料凭证详情
     */
    @GetMapping("/api/material/get/{materialDocumentId}")
    public MaterialDocumentDetailResponse getMaterialDocumentDetail(
            @PathVariable("materialDocumentId") String materialDocumentId) {
        log.info("物料凭证详情查询请求，标识: {}", materialDocumentId);
        try {
            MaterialDocumentDetailResponse response = materialDocumentService.getMaterialDocumentDetail(materialDocumentId);
            log.info("物料凭证详情查询完成，标识: {}", materialDocumentId);
            return response;
        } catch (Exception e) {
            log.error("物料凭证详情查询异常: {}", e.getMessage(), e);
            return new MaterialDocumentDetailResponse(false, "查询失败: " + e.getMessage(), null);
        }
    }

    /**
     * 调试接口：直接查询数据库中的物料凭证数量
     * @return 数据库中的记录数量
     */
    @GetMapping("/api/material/debug/count")
    public String getDebugCount() {
        try {
            // 直接通过mapper查询所有数据
            MaterialDocumentSearchRequest emptyRequest = new MaterialDocumentSearchRequest();
            MaterialDocumentSearchResponse response = materialDocumentService.searchMaterialDocuments(emptyRequest);
            
            return String.format("Debug Info: success=%s, message=%s, dataCount=%d",
                    response.isSuccess(),
                    response.getMessage(),
                    response.getData() != null ? response.getData().size() : 0);
        } catch (Exception e) {
            log.error("调试接口异常: {}", e.getMessage(), e);
            return "Debug Error: " + e.getMessage();
        }
    }

    /**
     * 调试接口：返回原始SQL查询结果信息
     * @return 原始查询信息
     */
    @GetMapping("/api/material/debug/raw")
    public String getRawDebugInfo() {
        try {
            MaterialDocumentSearchRequest request = new MaterialDocumentSearchRequest();
            log.info("=== 开始调试查询 ===");
            MaterialDocumentSearchResponse response = materialDocumentService.searchMaterialDocuments(request);
            log.info("=== 调试查询完成 ===");
            
            StringBuilder info = new StringBuilder();
            info.append("Raw Debug Info:\n");
            info.append("Success: ").append(response.isSuccess()).append("\n");
            info.append("Message: ").append(response.getMessage()).append("\n");
            info.append("Data: ").append(response.getData()).append("\n");
            info.append("Data Size: ").append(response.getData() != null ? response.getData().size() : "null").append("\n");
            
            if (response.getData() != null && !response.getData().isEmpty()) {
                info.append("First Record: ").append(response.getData().get(0)).append("\n");
            }
            
            return info.toString();
        } catch (Exception e) {
            log.error("原始调试接口异常: {}", e.getMessage(), e);
            return "Raw Debug Error: " + e.getMessage() + "\nStackTrace: " + e.getStackTrace()[0];
        }
    }
}