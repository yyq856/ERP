package webserver.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import webserver.mapper.BpRelationshipMapper;
import webserver.pojo.*;
import webserver.service.BpRelationshipService;

import java.time.LocalDate;

import java.util.*;

@Slf4j
@Service
public class BpRelationshipServiceImpl implements BpRelationshipService {

    @Autowired
    private BpRelationshipMapper bpRelationshipMapper;
    
    

    @Override
    public BpRelationshipResponse register(BpRelationshipRegisterRequest request) {
        try {
            // 参数验证
            if (request.getRelation() == null || !StringUtils.hasText(request.getRelation().getRelationShipCategory())) {
                return BpRelationshipResponse.error("关系类型不能为空");
            }
            
            if (request.getDefaultInfo() == null) {
                return BpRelationshipResponse.error("默认信息不能为空");
            }
            
            // 生成动态表单结构
            NodeStructure formStruct = generateFormStructure(request.getRelation().getRelationShipCategory());
            
            Map<String, Object> data = new HashMap<>();
            data.put("formStruct", formStruct);
            
            log.info("业务伙伴关系注册成功，关系类型: {}", request.getRelation().getRelationShipCategory());
            return BpRelationshipResponse.success(data, "成功");
            
        } catch (Exception e) {
            log.error("业务伙伴关系注册异常: {}", e.getMessage(), e);
            return BpRelationshipResponse.error("服务器内部错误");
        }
    }

    @Override
    public BpRelationshipResponse get(BpRelationshipGetRequest request) {
        try {
            // 参数验证
            if (!StringUtils.hasText(request.getRelationshipId())) {
                return BpRelationshipResponse.error("关系ID不能为空");
            }
            
            Long relationId;
            try {
                relationId = Long.parseLong(request.getRelationshipId());
            } catch (NumberFormatException e) {
                return BpRelationshipResponse.error("关系ID格式不正确");
            }
            
            // 查询业务伙伴关系
            BpRelationship relationship = bpRelationshipMapper.findByRelationId(relationId);
            if (relationship == null) {
                return BpRelationshipResponse.error("业务伙伴关系不存在");
            }
            
            // 构建响应数据
            Map<String, Object> content = new HashMap<>();
            
            // 基本信息
            Map<String, Object> basicInfo = new HashMap<>();
            Map<String, Object> relation = new HashMap<>();
            relation.put("relationShipCategory", relationship.getRelCategory());
            
            Map<String, Object> defaultInfo = new HashMap<>();
            defaultInfo.put("businessPartner1", relationship.getBp1().toString());
            defaultInfo.put("businessPartner2", relationship.getBp2().toString());
            defaultInfo.put("validFrom", relationship.getValidFrom().toString());
            defaultInfo.put("validTo", relationship.getValidTo().toString());
            
            basicInfo.put("relation", relation);
            basicInfo.put("default", defaultInfo);
            content.put("basicInfo", basicInfo);
            
            // 动态数据（示例）
            Map<String, Object> generalData = new HashMap<>();
            generalData.put("testField", "Sample Value");
            content.put("generalData", generalData);
            
            // 表单结构
            NodeStructure formStruct = generateFormStructure(relationship.getRelCategory());
            
            Map<String, Object> data = new HashMap<>();
            data.put("content", content);
            data.put("formStruct", formStruct);
            
            log.info("业务伙伴关系查询成功，ID: {}", relationId);
            return BpRelationshipResponse.success(data, "查询成功");
            
        } catch (Exception e) {
            log.error("业务伙伴关系查询异常: {}", e.getMessage(), e);
            return BpRelationshipResponse.error("服务器内部错误");
        }
    }

    @Override
    public BpRelationshipResponse edit(BpRelationshipEditRequest request) {
        try {
            // 参数验证
            if (request.getBpRelationshipData() == null || 
                request.getBpRelationshipData().getBasicInfo() == null) {
                return BpRelationshipResponse.error("基本信息不能为空");
            }
            
            BpRelationshipEditRequest.BpRelationshipData.BasicInfo basicInfo = 
                request.getBpRelationshipData().getBasicInfo();
            
            if (basicInfo.getRelation() == null || 
                !StringUtils.hasText(basicInfo.getRelation().getRelationShipCategory())) {
                return BpRelationshipResponse.error("关系类型不能为空");
            }
            
            if (basicInfo.getDefaultInfo() == null) {
                return BpRelationshipResponse.error("默认信息不能为空");
            }
            
            // 构建业务伙伴关系对象
            BpRelationship relationship = new BpRelationship();
            relationship.setRelCategory(basicInfo.getRelation().getRelationShipCategory());

            try {
                relationship.setBp1(Long.parseLong(basicInfo.getDefaultInfo().getBusinessPartner1()));
                relationship.setBp2(Long.parseLong(basicInfo.getDefaultInfo().getBusinessPartner2()));
                relationship.setValidFrom(LocalDate.parse(basicInfo.getDefaultInfo().getValidFrom()));
                relationship.setValidTo(LocalDate.parse(basicInfo.getDefaultInfo().getValidTo()));
            } catch (Exception e) {
                log.error("数据格式转换错误: {}", e.getMessage());
                return BpRelationshipResponse.error("数据格式不正确");
            }

            // 设置默认值（需要确保这些值在数据库中存在）
            relationship.setManagement(1);
            relationship.setDepartment("01");  // 使用可能存在的部门代码
            relationship.setFunction("01");    // 使用可能存在的功能代码
            
            String message;
            String responseMessage;
            
            // 判断是创建还是更新
            String id = request.getBpRelationshipData().getMeta() != null ? 
                       request.getBpRelationshipData().getMeta().getId() : null;
            
            if (StringUtils.hasText(id)) {
                // 更新操作
                relationship.setRelationId(Long.parseLong(id));
                int result = bpRelationshipMapper.updateRelationship(relationship);
                if (result > 0) {
                    message = "BP relation " + id + " has been updated successfully";
                    responseMessage = "操作成功";
                } else {
                    return BpRelationshipResponse.error("更新失败");
                }
            } else {
                // 创建操作
                int result = bpRelationshipMapper.insertRelationship(relationship);
                if (result > 0) {
                    id = relationship.getRelationId().toString();
                    message = "BP relation " + id + " has been created successfully";
                    responseMessage = "操作成功";
                } else {
                    return BpRelationshipResponse.error("创建失败");
                }
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("message", message);
            Map<String, Object> content = new HashMap<>();
            content.put("id", id);
            data.put("content", content);
            
            log.info("业务伙伴关系编辑成功，ID: {}", id);
            return BpRelationshipResponse.success(data, responseMessage);
            
        } catch (Exception e) {
            log.error("业务伙伴关系编辑异常: {}", e.getMessage(), e);
            return BpRelationshipResponse.error("服务器内部错误");
        }
    }
    
    /**
     * 根据关系类型生成动态表单结构
     * @param relationshipCategory 关系类型
     * @return 表单结构
     */
    private NodeStructure generateFormStructure(String relationshipCategory) {
        List<NodeStructure> children = new ArrayList<>();
        
        // 根据不同的关系类型生成不同的表单结构
        switch (relationshipCategory.toLowerCase()) {
            case "customer":
                children.add(NodeStructure.createLeaf("string", "customerCode", "Customer Code: "));
                children.add(NodeStructure.createLeaf("string", "customerName", "Customer Name: "));
                children.add(NodeStructure.createLeaf("string", "contactPerson", "Contact Person: "));
                break;
            case "supplier":
                children.add(NodeStructure.createLeaf("string", "supplierCode", "Supplier Code: "));
                children.add(NodeStructure.createLeaf("string", "supplierName", "Supplier Name: "));
                children.add(NodeStructure.createLeaf("string", "category", "Category: "));
                break;
            default:
                children.add(NodeStructure.createLeaf("string", "testField", "Test Field: "));
                children.add(NodeStructure.createLeaf("string", "description", "Description: "));
                break;
        }
        
        return NodeStructure.createDict("generalData", "General Data", children);
    }
}
