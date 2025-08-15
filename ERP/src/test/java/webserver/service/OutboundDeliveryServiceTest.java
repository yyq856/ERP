package webserver.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import webserver.pojo.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 出库交货单服务测试类
 */
@SpringBootTest
@ActiveProfiles("test")
public class OutboundDeliveryServiceTest {

    @Test
    public void testCreateOutboundDeliveryRequest() {
        // 测试创建请求DTO
        CreateOutboundDeliveryRequest request = new CreateOutboundDeliveryRequest();
        request.setSalesOrderIds(Arrays.asList("6001", "6002"));
        
        assertNotNull(request.getSalesOrderIds());
        assertEquals(2, request.getSalesOrderIds().size());
        assertEquals("6001", request.getSalesOrderIds().get(0));
    }

    @Test
    public void testOutboundDeliveryItemDTO() {
        // 测试物品DTO
        OutboundDeliveryItemDTO item = new OutboundDeliveryItemDTO();
        item.setItem("10");
        item.setMaterial("MAT-001");
        item.setPickingQuantity(new BigDecimal("100"));
        item.setPickingStatus("Completed");
        item.setConfirmationStatus("Not Confirmed");
        item.setItemType("Standard");
        item.setConversionRate(new BigDecimal("1.000"));
        
        assertEquals("10", item.getItem());
        assertEquals("MAT-001", item.getMaterial());
        assertEquals(new BigDecimal("100"), item.getPickingQuantity());
        assertEquals("Completed", item.getPickingStatus());
        assertEquals("Not Confirmed", item.getConfirmationStatus());
        assertEquals("Standard", item.getItemType());
        assertEquals(new BigDecimal("1.000"), item.getConversionRate());
    }

    @Test
    public void testOutboundDeliveryDetailResponse() {
        // 测试详情响应DTO
        OutboundDeliveryDetailResponse response = new OutboundDeliveryDetailResponse();
        
        // 创建Meta
        OutboundDeliveryDetailResponse.OutboundDeliveryDetail.Meta meta = 
            new OutboundDeliveryDetailResponse.OutboundDeliveryDetail.Meta();
        meta.setId("1");
        meta.setPosted(false);
        meta.setReadyToPost(true);
        
        // 创建Detail
        OutboundDeliveryDetailResponse.OutboundDeliveryDetail detail = 
            new OutboundDeliveryDetailResponse.OutboundDeliveryDetail();
        detail.setMeta(meta);
        detail.setPickingStatus("In Progress");
        detail.setOverallStatus("In Progress");
        detail.setGiStatus("In Progress");
        detail.setPriority("Normal Items");
        
        // 创建Items
        OutboundDeliveryItemDTO item = new OutboundDeliveryItemDTO();
        item.setItem("10");
        item.setMaterial("MAT-001");
        
        OutboundDeliveryDetailResponse.OutboundDeliveryItems items = 
            new OutboundDeliveryDetailResponse.OutboundDeliveryItems();
        items.setItems(Arrays.asList(item));
        
        response.setDetail(detail);
        response.setItems(items);
        
        assertNotNull(response.getDetail());
        assertNotNull(response.getItems());
        assertEquals("1", response.getDetail().getMeta().getId());
        assertEquals(false, response.getDetail().getMeta().getPosted());
        assertEquals(true, response.getDetail().getMeta().getReadyToPost());
        assertEquals("In Progress", response.getDetail().getPickingStatus());
        assertEquals(1, response.getItems().getItems().size());
    }

    @Test
    public void testValidateItemsResponse() {
        // 测试验证响应DTO
        ValidateItemsResponse response = new ValidateItemsResponse();
        response.setSuccess(true);
        response.setMessage("验证成功");
        
        ValidateItemsResponse.ValidationResult result = new ValidateItemsResponse.ValidationResult();
        result.setAllDataLegal(1);
        result.setBadRecordIndices(Arrays.asList());
        
        ValidateItemsResponse.ValidateItemsData data = new ValidateItemsResponse.ValidateItemsData();
        data.setResult(result);
        
        response.setData(data);
        
        assertTrue(response.isSuccess());
        assertEquals("验证成功", response.getMessage());
        assertEquals(1, response.getData().getResult().getAllDataLegal());
        assertTrue(response.getData().getResult().getBadRecordIndices().isEmpty());
    }

    @Test
    public void testPostGIsByIdRequest() {
        // 测试过账请求DTO
        PostGIsByIdRequest request = new PostGIsByIdRequest();
        request.setDeliveryIds(Arrays.asList("1", "2", "3"));
        
        assertNotNull(request.getDeliveryIds());
        assertEquals(3, request.getDeliveryIds().size());
        assertEquals("1", request.getDeliveryIds().get(0));
    }

    @Test
    public void testGetOutboundDeliverySummaryRequest() {
        // 测试汇总请求DTO
        GetOutboundDeliverySummaryRequest request = new GetOutboundDeliverySummaryRequest();
        request.setOverallStatus("In Progress");
        request.setCreatedBy("admin");
        
        assertEquals("In Progress", request.getOverallStatus());
        assertEquals("admin", request.getCreatedBy());
    }
}
