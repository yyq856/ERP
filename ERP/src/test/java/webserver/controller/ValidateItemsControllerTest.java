package webserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import webserver.pojo.ItemsTabQueryRequest;
import webserver.pojo.ItemsTabQueryResponse;
import webserver.service.ValidateItemsService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ValidateItemsController测试类
 */
@WebMvcTest(ValidateItemsController.class)
class ValidateItemsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ValidateItemsService validateItemsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testInquiryItemsTabQuery_Success() throws Exception {
        // 准备测试数据
        List<ItemsTabQueryRequest> request = createTestRequest();
        ItemsTabQueryResponse mockResponse = createMockResponse();

        // Mock服务层
        when(validateItemsService.processItemsTabQuery(any(), eq("inquiry")))
                .thenReturn(mockResponse);

        // 执行测试
        mockMvc.perform(post("/api/app/inquiry/items-tab-query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("批量验证成功"))
                .andExpect(jsonPath("$.data.result.allDataLegal").value(1))
                .andExpect(jsonPath("$.data.breakdowns").isArray())
                .andExpect(jsonPath("$.data.breakdowns[0].item").value("1"))
                .andExpect(jsonPath("$.data.breakdowns[0].material").value("1001"));
    }

    @Test
    void testQuotationItemsTabQuery_Success() throws Exception {
        // 准备测试数据
        List<ItemsTabQueryRequest> request = createTestRequest();
        ItemsTabQueryResponse mockResponse = createMockResponse();

        // Mock服务层
        when(validateItemsService.processItemsTabQuery(any(), eq("quotation")))
                .thenReturn(mockResponse);

        // 执行测试
        mockMvc.perform(post("/api/app/quotation/items-tab-query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testSalesOrderItemsTabQuery_Success() throws Exception {
        // 准备测试数据
        List<ItemsTabQueryRequest> request = createTestRequest();
        ItemsTabQueryResponse mockResponse = createMockResponse();

        // Mock服务层
        when(validateItemsService.processItemsTabQuery(any(), eq("so")))
                .thenReturn(mockResponse);

        // 执行测试
        mockMvc.perform(post("/api/app/so/items-tab-query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testBillingItemsTabQuery_Success() throws Exception {
        // 准备测试数据
        List<ItemsTabQueryRequest> request = createTestRequest();
        ItemsTabQueryResponse mockResponse = createMockResponse();

        // Mock服务层
        when(validateItemsService.processItemsTabQuery(any(), eq("billing")))
                .thenReturn(mockResponse);

        // 执行测试
        mockMvc.perform(post("/api/app/billing/items-tab-query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testItemsTabQuery_EmptyRequest() throws Exception {
        // 准备空请求
        List<ItemsTabQueryRequest> emptyRequest = new ArrayList<>();
        ItemsTabQueryResponse errorResponse = new ItemsTabQueryResponse(false, "请求数据不能为空", null);

        // Mock服务层
        when(validateItemsService.processItemsTabQuery(any(), eq("inquiry")))
                .thenReturn(errorResponse);

        // 执行测试
        mockMvc.perform(post("/api/app/inquiry/items-tab-query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("请求数据不能为空"));
    }

    @Test
    void testItemsTabQuery_ServiceException() throws Exception {
        // 准备测试数据
        List<ItemsTabQueryRequest> request = createTestRequest();

        // Mock服务层抛出异常
        when(validateItemsService.processItemsTabQuery(any(), eq("inquiry")))
                .thenThrow(new RuntimeException("数据库连接异常"));

        // 执行测试
        mockMvc.perform(post("/api/app/inquiry/items-tab-query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("验证失败，请检查输入数据"));
    }

    /**
     * 创建测试请求数据
     */
    private List<ItemsTabQueryRequest> createTestRequest() {
        List<ItemsTabQueryRequest> request = new ArrayList<>();
        
        ItemsTabQueryRequest item = new ItemsTabQueryRequest();
        item.setItem("10");
        item.setMaterial("1001");
        item.setOrderQuantity("2");
        item.setOrderQuantityUnit("PC");
        item.setDescription("标准电脑主板");
        item.setReqDelivDate("2025-02-15");
        item.setPricingDate("2025-01-13");
        item.setOrderProbability("100");
        
        // 添加定价元素
        List<ItemsTabQueryRequest.PricingElementRequest> pricingElements = new ArrayList<>();
        ItemsTabQueryRequest.PricingElementRequest element = new ItemsTabQueryRequest.PricingElementRequest();
        element.setCnty("BASE");
        element.setName("基础价格");
        element.setAmount("1200.00");
        element.setCity("CNY");
        element.setPer("1");
        element.setUom("PC");
        element.setConditionValue("2400.00");
        element.setCurr("CNY");
        element.setStat(true);
        pricingElements.add(element);
        
        item.setPricingElements(pricingElements);
        request.add(item);
        
        return request;
    }

    /**
     * 创建模拟响应数据
     */
    private ItemsTabQueryResponse createMockResponse() {
        // 创建验证结果
        ItemsTabQueryResponse.ValidationResult result = new ItemsTabQueryResponse.ValidationResult();
        result.setAllDataLegal(1);
        result.setBadRecordIndices(new ArrayList<>());

        // 创建总体数据
        ItemsTabQueryResponse.GeneralData generalData = new ItemsTabQueryResponse.GeneralData();
        generalData.setNetValue("2400.00");
        generalData.setNetValueUnit("CNY");
        generalData.setExpectOralVal("2712.00");
        generalData.setExpectOralValUnit("CNY");

        // 创建物品明细
        List<ItemsTabQueryResponse.ItemBreakdown> breakdowns = new ArrayList<>();
        ItemsTabQueryResponse.ItemBreakdown breakdown = new ItemsTabQueryResponse.ItemBreakdown();
        breakdown.setItem("1");
        breakdown.setMaterial("1001");
        breakdown.setOrderQuantity("2");
        breakdown.setOrderQuantityUnit("PC");
        breakdown.setDescription("标准电脑主板");
        breakdown.setReqDelivDate("2025-02-15");
        breakdown.setNetValue(2400.0);
        breakdown.setNetValueUnit("CNY");
        breakdown.setTaxValue(312.0);
        breakdown.setTaxValueUnit("CNY");
        breakdown.setPricingDate("2025-01-13");
        breakdown.setOrderProbability("100");
        breakdown.setPricingElements(new ArrayList<>());
        
        breakdowns.add(breakdown);

        // 创建响应数据
        ItemsTabQueryResponse.ItemsTabQueryData data = new ItemsTabQueryResponse.ItemsTabQueryData();
        data.setResult(result);
        data.setGeneralData(generalData);
        data.setBreakdowns(breakdowns);

        return new ItemsTabQueryResponse(true, "批量验证成功", data);
    }
}