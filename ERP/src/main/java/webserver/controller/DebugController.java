package webserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class DebugController {

    /**
     * 最简单的测试接口 - 测试基本连通性
     */
    @GetMapping("/debug/hello")
    public Map<String, Object> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Hello, API is working!");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 测试POST请求和JSON解析
     */
    @PostMapping("/debug/echo")
    public Map<String, Object> echo(@RequestBody Map<String, Object> request) {
        log.info("收到请求: {}", request);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Echo test successful");
        response.put("received", request);
        return response;
    }

    /**
     * 测试字符串数组请求
     */
    @PostMapping("/debug/string-array")
    public Map<String, Object> testStringArray(@RequestBody String[] request) {
        log.info("收到字符串数组: {}", (Object) request);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "String array test successful");
        response.put("received", request);
        response.put("count", request.length);
        return response;
    }
}