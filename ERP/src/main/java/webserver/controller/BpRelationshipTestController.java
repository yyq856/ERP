package webserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import webserver.mapper.BpRelationshipMapper;
import webserver.pojo.BpRelationship;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class BpRelationshipTestController {

    @Autowired
    private BpRelationshipMapper bpRelationshipMapper;

    /**
     * 测试数据库连接和插入
     */
    @PostMapping("/api/test/bp-relationship")
    public Map<String, Object> testBpRelationship(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 创建测试数据
            BpRelationship relationship = new BpRelationship();
            relationship.setRelCategory("customer");
            relationship.setBp1(1L);
            relationship.setBp2(2L);
            relationship.setValidFrom(LocalDate.parse("2024-01-01"));
            relationship.setValidTo(LocalDate.parse("2024-12-31"));
            relationship.setManagement(1);
            relationship.setDepartment("01");
            relationship.setFunction("01");
            
            log.info("尝试插入业务伙伴关系: {}", relationship);
            
            // 插入数据
            int insertResult = bpRelationshipMapper.insertRelationship(relationship);
            
            result.put("success", true);
            result.put("message", "插入成功");
            result.put("insertResult", insertResult);
            result.put("generatedId", relationship.getRelationId());
            
            log.info("业务伙伴关系插入成功，ID: {}", relationship.getRelationId());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "插入失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            
            log.error("业务伙伴关系插入失败: {}", e.getMessage(), e);
        }
        return result;
    }

    /**
     * 测试查询
     */
    @GetMapping("/api/test/bp-relationship/{id}")
    public Map<String, Object> testGetBpRelationship(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long relationId = Long.parseLong(id);
            BpRelationship relationship = bpRelationshipMapper.findByRelationId(relationId);
            
            result.put("success", true);
            result.put("message", "查询成功");
            result.put("relationship", relationship);
            
            log.info("业务伙伴关系查询成功: {}", relationship);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
            
            log.error("业务伙伴关系查询失败: {}", e.getMessage(), e);
        }
        return result;
    }
}
