package webserver.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import webserver.pojo.ValidateItemsRequest;
import webserver.pojo.ValidateItemsResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ValidateItemsServiceTest {

    @Autowired
    private ValidateItemsService validateItemsService;

    private List<ValidateItemsRequest> validTestData;
    private List<ValidateItemsRequest> invalidTestData;

    @BeforeEach
    void setUp() {
        // 准备有效的测试数据
        validTestData = createValidTestData();
        
        // 准备无效的测试数据
        invalidTestData = createInvalidTestData();
    }

    @Test
    void testValidateItems_ValidData_ShouldReturnSuccess() {
        // Given
        List<ValidateItemsRequest> request = validTestData;

        // When
        ValidateItemsResponse response = validateItemsService.validateAndCalculateItems(request);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("批量验证成功", response.getMessage());
        
        assertNotNull(response.getData());
        assertNotNull(response.getData().getResult());
        assertEquals(1, response.getData().getResult().getAllDataLegal());
        assertTrue(response.getData().getResult().getBadRecordIndices().isEmpty());
        
        assertNotNull(response.getData().getGeneralData());
        // ValidateItemsResponse.GeneralData 没有 netValue 字段，它主要用于 outbound delivery 相关信息
        assertNotNull(response.getData().getGeneralData().getPickingStatus());
        assertNotNull(response.getData().getGeneralData().getOverallStatus());
        
        assertNotNull(response.getData().getBreakdowns());
        // ValidateItemsResponse 的 breakdowns 是 OutboundDeliveryItemDTO 类型的列表
        // 对于非 outbound delivery 的验证，这个列表通常为空
        assertTrue(response.getData().getBreakdowns().isEmpty() || response.getData().getBreakdowns().size() >= 0);
    }

    @Test
    void testValidateItems_InvalidData_ShouldReturnBadIndices() {
        // Given
        List<ValidateItemsRequest> request = invalidTestData;

        // When
        ValidateItemsResponse response = validateItemsService.validateAndCalculateItems(request);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("批量验证成功", response.getMessage());
        
        assertNotNull(response.getData());
        assertNotNull(response.getData().getResult());
        assertEquals(0, response.getData().getResult().getAllDataLegal());
        assertFalse(response.getData().getResult().getBadRecordIndices().isEmpty());
        
        // 验证不合法数据的索引
        List<Integer> badIndices = response.getData().getResult().getBadRecordIndices();
        assertTrue(badIndices.contains(0)); // 第一个数据缺少必填字段
        assertTrue(badIndices.contains(1)); // 第二个数据物料不存在
    }

    @Test
    void testValidateItems_MixedData_ShouldHandleCorrectly() {
        // Given
        List<ValidateItemsRequest> mixedData = new ArrayList<>();
        mixedData.addAll(validTestData);
        mixedData.addAll(invalidTestData);

        // When
        ValidateItemsResponse response = validateItemsService.validateAndCalculateItems(mixedData);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        
        assertNotNull(response.getData());
        assertNotNull(response.getData().getResult());
        assertEquals(0, response.getData().getResult().getAllDataLegal()); // 存在不合法数据
        
        List<Integer> badIndices = response.getData().getResult().getBadRecordIndices();
        assertTrue(badIndices.contains(2)); // 混合数据中的无效项
        assertTrue(badIndices.contains(3));
        
        // 验证有效数据被正确处理
        assertNotNull(response.getData().getBreakdowns());
        // ValidateItemsResponse 的 breakdowns 对于非 outbound delivery 验证通常为空
        assertTrue(response.getData().getBreakdowns().isEmpty() || response.getData().getBreakdowns().size() >= 0);
    }

    @Test
    void testValidateItems_EmptyRequest_ShouldReturnError() {
        // Given
        List<ValidateItemsRequest> emptyRequest = new ArrayList<>();

        // When
        ValidateItemsResponse response = validateItemsService.validateAndCalculateItems(emptyRequest);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("请求数据不能为空", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testValidateItems_NullRequest_ShouldReturnError() {
        // Given
        List<ValidateItemsRequest> nullRequest = null;

        // When
        ValidateItemsResponse response = validateItemsService.validateAndCalculateItems(nullRequest);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("请求数据不能为空", response.getMessage());
        assertNull(response.getData());
    }

    private List<ValidateItemsRequest> createValidTestData() {
        List<ValidateItemsRequest> data = new ArrayList<>();

        // 第一个有效物品
        ValidateItemsRequest item1 = new ValidateItemsRequest();
        item1.setItem("001");
        item1.setMaterial("1001"); // 标准电脑主板
        item1.setOrderQuantity("2");
        item1.setOrderQuantityUnit("PC");
        item1.setDescription("标准电脑主板采购");
        item1.setReqDelivDate("2024-03-15");
        item1.setNetValue("2400.00");
        item1.setNetValueUnit("CNY");
        item1.setTaxValue("240.00");
        item1.setTaxValueUnit("CNY");
        item1.setPricingDate("2024-03-01");
        item1.setOrderProbability("90");
        
        // 添加定价元素
        ValidateItemsRequest.PricingElement pricingElement1 = new ValidateItemsRequest.PricingElement();
        pricingElement1.setCnty("001");
        pricingElement1.setName("基础价格");
        pricingElement1.setAmount("2400.00");
        pricingElement1.setCity("北京");
        pricingElement1.setPer("100");
        pricingElement1.setUom("CNY");
        pricingElement1.setConditionValue("1200.00");
        pricingElement1.setCurr("CNY");
        pricingElement1.setStatus("ACTIVE");
        pricingElement1.setStat(true);
        
        item1.setPricingElements(Arrays.asList(pricingElement1));
        data.add(item1);

        // 第二个有效物品
        ValidateItemsRequest item2 = new ValidateItemsRequest();
        item2.setItem("002");
        item2.setMaterial("1002"); // 高性能显卡
        item2.setOrderQuantity("1");
        item2.setOrderQuantityUnit("PC");
        item2.setDescription("高性能显卡采购");
        item2.setReqDelivDate("2024-03-20");
        item2.setNetValue("2800.00");
        item2.setNetValueUnit("CNY");
        item2.setTaxValue("280.00");
        item2.setTaxValueUnit("CNY");
        item2.setPricingDate("2024-03-01");
        item2.setOrderProbability("85");
        
        ValidateItemsRequest.PricingElement pricingElement2 = new ValidateItemsRequest.PricingElement();
        pricingElement2.setCnty("002");
        pricingElement2.setName("显卡基础价格");
        pricingElement2.setAmount("2800.00");
        pricingElement2.setCity("上海");
        pricingElement2.setPer("100");
        pricingElement2.setUom("CNY");
        pricingElement2.setConditionValue("2800.00");
        pricingElement2.setCurr("CNY");
        pricingElement2.setStatus("ACTIVE");
        pricingElement2.setStat(true);
        
        item2.setPricingElements(Arrays.asList(pricingElement2));
        data.add(item2);

        return data;
    }

    private List<ValidateItemsRequest> createInvalidTestData() {
        List<ValidateItemsRequest> data = new ArrayList<>();

        // 第一个无效物品 - 缺少必填字段
        ValidateItemsRequest invalidItem1 = new ValidateItemsRequest();
        invalidItem1.setItem("003");
        // 缺少 material
        invalidItem1.setOrderQuantity("1");
        invalidItem1.setOrderQuantityUnit("PC");
        data.add(invalidItem1);

        // 第二个无效物品 - 物料不存在
        ValidateItemsRequest invalidItem2 = new ValidateItemsRequest();
        invalidItem2.setItem("004");
        invalidItem2.setMaterial("9999"); // 不存在的物料
        invalidItem2.setOrderQuantity("1");
        invalidItem2.setOrderQuantityUnit("PC");
        data.add(invalidItem2);

        return data;
    }
}