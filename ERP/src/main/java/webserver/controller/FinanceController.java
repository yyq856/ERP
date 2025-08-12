package webserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import webserver.common.Response;
import webserver.service.FinanceService;
import webserver.pojo.SearchOpenItemsRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {
    
    @Autowired
    private FinanceService financeService;
    
    /**
     * 搜索未清项接口
     * 根据筛选条件搜索某公司的未清项
     */
    @PostMapping("/searchOpenItems")
    public Response<Map<String, Object>> searchOpenItems(@RequestBody SearchOpenItemsRequest request) {
        try {
            Map<String, Object> result = financeService.searchOpenItems(request);
            return Response.success(result);
        } catch (Exception e) {
            return Response.error("Failed to search open items: " + e.getMessage());
        }
    }
}
