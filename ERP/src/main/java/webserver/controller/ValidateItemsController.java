package webserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import webserver.pojo.ValidateItemsRequest;
import webserver.pojo.ValidateItemsResponse;
import webserver.service.ValidateItemsService;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class ValidateItemsController {

    @Autowired
    private ValidateItemsService validateItemsService;

    /**
     * 物品验证服务接口
     * 接口地址：POST /items-tab-query
     * @param request 物品验证请求列表
     * @return 验证结果
     */
    @PostMapping(value = {"/items-tab-query", "/inquiry/items-tab-query"},
                 consumes = "application/json",
                 produces = "application/json")
    public ValidateItemsResponse validateItems(@RequestBody List<ValidateItemsRequest> request) {
        log.info("物品验证请求，数量: {}", request != null ? request.size() : 0);
        try {
            ValidateItemsResponse response = validateItemsService.validateAndCalculateItems(request);
            log.info("物品验证完成，结果: 成功={}, 不合法数据数量={}", 
                    response.getData() != null && response.getData().getResult() != null ? 
                    response.getData().getResult().getAllDataLegal() : "unknown",
                    response.getData() != null && response.getData().getResult() != null && 
                    response.getData().getResult().getBadRecordIndices() != null ? 
                    response.getData().getResult().getBadRecordIndices().size() : 0);
            return response;
        } catch (Exception e) {
            log.error("物品验证异常: {}", e.getMessage(), e);
            return new ValidateItemsResponse(false, "验证失败，请检查输入数据", null);
        }
    }
}