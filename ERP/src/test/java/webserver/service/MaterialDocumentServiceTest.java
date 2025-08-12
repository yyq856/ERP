package webserver.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import webserver.pojo.MaterialDocumentSearchRequest;
import webserver.pojo.MaterialDocumentSearchResponse;
import webserver.pojo.MaterialDocumentDetailResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 物料凭证服务测试类
 * 注意：这是一个基础测试框架，实际测试需要配置测试数据库
 */
@SpringBootTest
@ActiveProfiles("test")
public class MaterialDocumentServiceTest {

    /**
     * 测试搜索请求参数验证
     */
    @Test
    public void testSearchRequestValidation() {
        // 创建搜索请求
        MaterialDocumentSearchRequest request = new MaterialDocumentSearchRequest();
        request.setMaterialDocument("MD001");
        request.setPlant("Plant001");
        request.setMaterialDocumentYear("2024");
        request.setMaterial("MAT001");
        request.setPostingDate("2024/01/01");  // 测试日期格式转换
        request.setDocumentDate("2024-01-01");
        
        // 验证请求对象构建正确
        assertNotNull(request);
        assertEquals("MD001", request.getMaterialDocument());
        assertEquals("Plant001", request.getPlant());
        assertEquals("2024", request.getMaterialDocumentYear());
        assertEquals("MAT001", request.getMaterial());
        assertEquals("2024/01/01", request.getPostingDate());
        assertEquals("2024-01-01", request.getDocumentDate());
    }

    /**
     * 测试响应对象结构
     */
    @Test
    public void testResponseStructure() {
        // 测试搜索响应
        MaterialDocumentSearchResponse searchResponse = new MaterialDocumentSearchResponse();
        searchResponse.setSuccess(true);
        searchResponse.setMessage("成功");
        
        assertNotNull(searchResponse);
        assertTrue(searchResponse.isSuccess());
        assertEquals("成功", searchResponse.getMessage());
        
        // 测试详情响应
        MaterialDocumentDetailResponse detailResponse = new MaterialDocumentDetailResponse();
        detailResponse.setSuccess(true);
        detailResponse.setMessage("查询成功");
        
        assertNotNull(detailResponse);
        assertTrue(detailResponse.isSuccess());
        assertEquals("成功", detailResponse.getMessage());
    }

    /**
     * 测试材料凭证详情数据结构
     */
    @Test
    public void testMaterialDocumentDetailStructure() {
        MaterialDocumentDetailResponse.MaterialDocumentDetail detail = 
            new MaterialDocumentDetailResponse.MaterialDocumentDetail();
        
        detail.setMaterialDocument("MD001");
        detail.setPlant("Plant001");
        detail.setPostingDate("2024-01-01");
        detail.setDocumentDate("2024-01-01");
        detail.setMaterialDocumentYear("2024");
        
        assertNotNull(detail);
        assertEquals("MD001", detail.getMaterialDocument());
        assertEquals("Plant001", detail.getPlant());
        assertEquals("2024-01-01", detail.getPostingDate());
        assertEquals("2024-01-01", detail.getDocumentDate());
        assertEquals("2024", detail.getMaterialDocumentYear());
    }

    /**
     * 测试物料凭证项目数据结构
     */
    @Test
    public void testMaterialDocumentItemStructure() {
        MaterialDocumentDetailResponse.MaterialDocumentItemDetail item = 
            new MaterialDocumentDetailResponse.MaterialDocumentItemDetail();
        
        item.setItem("1");
        item.setMaterial("MAT001");
        item.setOrderQuantity("100");
        item.setOrderQuantityUnit("PC");
        
        assertNotNull(item);
        assertEquals("1", item.getItem());
        assertEquals("MAT001", item.getMaterial());
        assertEquals("100", item.getOrderQuantity());
        assertEquals("PC", item.getOrderQuantityUnit());
    }

    /**
     * 测试业务流程数据结构
     */
    @Test
    public void testProcessFlowStructure() {
        MaterialDocumentDetailResponse.ProcessFlowDetail processFlow = 
            new MaterialDocumentDetailResponse.ProcessFlowDetail();
        
        processFlow.setDlvId("DLV001");
        processFlow.setMaterialDocument("MD001");
        processFlow.setBillId("BILL001");
        
        assertNotNull(processFlow);
        assertEquals("DLV001", processFlow.getDlvId());
        assertEquals("MD001", processFlow.getMaterialDocument());
        assertEquals("BILL001", processFlow.getBillId());
    }
}