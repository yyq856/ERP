package webserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import webserver.common.Response;
import webserver.pojo.SearchRequest;
import webserver.service.SearchService;

import java.util.List;
import java.util.Map;

@RestController
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
}
