package webserver.e2e;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 端到端 POST 接口测试框架
 * - 自动发现所有 @PostMapping/@RequestMapping(method=POST) 映射
 * - 解析类级 @RequestMapping 路径前缀
 * - 为已配置示例请求体的接口生成动态测试（未配置样例的端点会被跳过）
 *
 * 注意：
 * - 这是测试框架骨架，默认只对有示例请求体的端点执行 200 断言。
 *   其它端点会被列出但不会执行（使用 Assumptions 跳过）。
 * - 如需对更多 POST 端点做真实 E2E 校验，请在 post-bodies.json 中添加该端点的请求体样例。
 */
@SpringBootTest
@AutoConfigureMockMvc
public class PostEndpointsE2ETest {

    @Value("${e2e.base-url:}")
    private String baseUrl;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Autowired
    private MockMvc mockMvc;

    /**
     * 从 JSON 资源文件加载示例请求体（支持每个路径多个用例）。
     * 结构：
     *   - 单个用例：value 为对象或数组
     *   - 多个用例：value 为 { "cases": [ {...}, {...} ] }
     */
    private static Map<String, java.util.List<String>> loadSampleBodies() {
        try (java.io.InputStream is = PostEndpointsE2ETest.class.getClassLoader()
                .getResourceAsStream("e2e/post-bodies.json")) {
            if (is == null) return java.util.Collections.emptyMap();
            String json = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(json);
            java.util.Map<String, java.util.List<String>> out = new java.util.HashMap<>();
            var fields = root.fields();
            while (fields.hasNext()) {
                var entry = fields.next();
                String path = entry.getKey();
                com.fasterxml.jackson.databind.JsonNode node = entry.getValue();
                java.util.List<String> cases = new java.util.ArrayList<>();
                if (node.isObject() && node.has("cases") && node.get("cases").isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode c : node.get("cases")) {
                        cases.add(mapper.writeValueAsString(c));
                    }
                } else {
                    cases.add(mapper.writeValueAsString(node));
                }
                out.put(path, cases);
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException("加载 e2e/post-bodies.json 失败", e);
        }
    }

    private static final Map<String, java.util.List<String>> sampleBodies = loadSampleBodies();

    /**
     * 列出所有 POST 端点及其类级前缀，便于检查覆盖情况。
     */
    @Test
    void listAllPostMappings() {
        Map<String, List<String>> grouped = collectPostEndpoints().stream()
                .collect(Collectors.groupingBy(
                        info -> info.classPrefix,
                        TreeMap::new,
                        Collectors.mapping(i -> i.fullPath, Collectors.toList())));

        grouped.forEach((prefix, paths) -> {
            System.out.println("[Class Prefix] " + prefix);
            paths.stream().sorted().forEach(p -> System.out.println("  POST " + p));
        });
    }

    /**
     * 为有示例请求体的端点生成动态测试。
     * 未配置样例的端点将被跳过（Assumptions）。
     */
    @TestFactory
    Stream<DynamicTest> postEndpointDynamicTests() {
        List<EndpointInfo> endpoints = collectPostEndpoints();
        return endpoints.stream().flatMap(ep -> {
            List<String> bodies = sampleBodies.getOrDefault(ep.fullPath, java.util.List.of());
            if (bodies.isEmpty()) {
                return Stream.of(DynamicTest.dynamicTest(
                        "POST " + ep.fullPath + " [" + (ep.classPrefix.isBlank()?"/":ep.classPrefix) + "] - SKIPPED",
                        () -> Assumptions.assumeTrue(false, () -> "未配置样例请求体，跳过：" + ep.fullPath)
                ));
            }
            return bodies.stream().map((body) -> DynamicTest.dynamicTest(
                    "POST " + ep.fullPath + " [case]",
                    () -> {
                        if (StringUtils.hasText(baseUrl)) {
                            var headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            var req = new HttpEntity<>(body, headers);
                            var rt = new RestTemplate();
                            var resp = rt.postForEntity(baseUrl + ep.fullPath, req, String.class);
                            Assertions.assertEquals(200, resp.getStatusCode().value());
                        } else {
                            mockMvc.perform(
                                    post(ep.fullPath)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(body)
                            ).andExpect(status().isOk());
                        }
                    }
            ));
        });
    }

    // ====== 内部工具方法/结构 ======

    private List<EndpointInfo> collectPostEndpoints() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
        List<EndpointInfo> list = new ArrayList<>();
        handlerMethods.forEach((info, method) -> {
            // 只取 POST 方法
            boolean isPost = info.getMethodsCondition().getMethods().contains(RequestMethod.POST);
            if (!isPost) return;

            // 类级前缀（可能为空）
            String classPrefix = extractClassPrefix(method.getBeanType());

            // 路径（可能有多个 pattern）
            Set<String> paths = extractPatternValues(info);
            for (String p : paths) {
                String fullPath = normalizePath(classPrefix, p);
                list.add(new EndpointInfo(method.getBeanType().getSimpleName(), classPrefix, method.getMethod().getName(), fullPath));
            }
        });
        return list.stream().sorted(Comparator.comparing(e -> e.fullPath)).toList();
    }

    private String extractClassPrefix(Class<?> controllerClass) {
        RequestMapping rm = controllerClass.getAnnotation(RequestMapping.class);
        if (rm == null) return "";
        String[] values = rm.value().length > 0 ? rm.value() : rm.path();
        if (values == null || values.length == 0) return "";
        // 仅取第一个作为前缀（如需支持多前缀，可展开）
        return ensureStartsWithSlash(values[0]);
    }

    private Set<String> extractPatternValues(RequestMappingInfo info) {
        // Spring Framework 6 使用 PathPatterns；getPatternValues() 可直接拿到字符串
        if (info.getPathPatternsCondition() != null) {
            return info.getPathPatternsCondition().getPatternValues();
        }
        // 兜底：无路径时返回根
        return Set.of("/");
    }

    private String normalizePath(String prefix, String methodPath) {
        String p = ensureStartsWithSlash(prefix == null ? "" : prefix);
        String m = ensureStartsWithSlash(methodPath == null ? "" : methodPath);
        if ("/".equals(p)) p = ""; // 类前缀为根时当作无前缀
        String joined = (p + "/" + m).replaceAll("/+", "/");
        return joined.equals("") ? "/" : joined;
    }

    private String ensureStartsWithSlash(String s) {
        if (s == null || s.isBlank()) return "/";
        return s.startsWith("/") ? s : "/" + s;
    }

    private record EndpointInfo(String className, String classPrefix, String methodName, String fullPath) {}
}
