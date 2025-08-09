package webserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import webserver.common.Response;
import webserver.pojo.SearchRequest;
import webserver.service.SearchService;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @PostMapping
    public Response<List<Map<String, Object>>> search(@RequestBody SearchRequest request) {
        try {
            List<Map<String, Object>> result = searchService.search(request);
            return Response.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 业务伙伴搜索辅助接口 (用于下拉选择/输入建议)
     * @param query 查询关键字
     * @return 搜索建议列表
     */
    @GetMapping("/business-partner")
    public Response<List<Map<String, Object>>> searchBusinessPartner(@RequestParam(required = false) String query) {
        try {
            // 这里可以调用专门的搜索服务来提供业务伙伴搜索建议
            // 暂时返回空列表，可根据实际需求实现
            return Response.success(java.util.Collections.emptyList());
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 关系搜索辅助接口
     * @param query 查询关键字
     * @return 关系类型列表
     */
    @GetMapping("/relation")
    public Response<List<Map<String, Object>>> searchRelation(@RequestParam(required = false) String query) {
        try {
            // 这里可以调用专门的关系搜索服务
            // 暂时返回空列表，可根据实际需求实现
            return Response.success(java.util.Collections.emptyList());
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("搜索失败: " + e.getMessage());
        }
    }
}
