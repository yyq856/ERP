package webserver.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import webserver.mapper.BusinessPartnerMapper;
import webserver.pojo.*;
import webserver.service.BusinessPartnerService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class BusinessPartnerServiceImpl implements BusinessPartnerService {

    @Autowired
    private BusinessPartnerMapper businessPartnerMapper;

    @Override
    public BpResponse searchBusinessPartner(BpSearchRequest request) {
        try {
            // 参数验证
            if (!StringUtils.hasText(request.getCustomerId())) {
                return BpResponse.error("INVALID_INPUT", "Customer ID is required");
            }

            // 查询业务伙伴
            BusinessPartner bp = businessPartnerMapper.findByCustomerId(request.getCustomerId());
            
            if (bp == null) {
                return BpResponse.error("BP_NOT_FOUND", 
                    "Business partner with ID '" + request.getCustomerId() + "' not found.");
            }

            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("bpId", bp.getCustomerId().toString());
            data.put("name", bp.getName());
            data.put("status", "Active"); // 假设状态，可根据实际需求调整

            return BpResponse.success(data, "Business partner found successfully.");

        } catch (Exception e) {
            log.error("业务伙伴查询异常: {}", e.getMessage(), e);
            return BpResponse.error("INTERNAL_ERROR", "Internal server error occurred");
        }
    }

    @Override
    public BpResponse createBusinessPartner(BpCreateRequest request) {
        try {
            // 参数验证
            if (request.getCustomer() == null) {
                return BpResponse.error("INVALID_INPUT", "Customer data is required");
            }

            BpCreateRequest.Customer customer = request.getCustomer();
            
            // 验证必填字段
            if (customer.getName() == null || !StringUtils.hasText(customer.getName().getName())) {
                return BpResponse.error("INVALID_INPUT", "Customer name is required");
            }

            if (customer.getAddress() == null) {
                return BpResponse.error("INVALID_INPUT", "Address information is required");
            }

            // 构建业务伙伴对象
            BusinessPartner bp = new BusinessPartner();
            bp.setTitle(customer.getName().getTitle());
            bp.setName(customer.getName().getName());
            bp.setStreet(customer.getAddress().getStreet());
            bp.setCity(customer.getAddress().getCity());
            bp.setPostalCode(customer.getAddress().getPostalCode());
            bp.setCountry(customer.getAddress().getCountry());
            
            // 设置默认值（根据实际业务需求调整）
            bp.setLanguage("EN");
            bp.setRegion("01");
            bp.setCompanyCode("1000");
            bp.setReconciliationAccount("140000");
            bp.setSortKey("001");
            bp.setSalesOrg("1000");
            bp.setChannel(10);
            bp.setDivision("01");
            bp.setCurrency("USD");
            bp.setSalesDistrict("000001");
            bp.setPriceGroup("01");
            bp.setCustomerGroup("01");
            bp.setDeliveryPriority("02");
            bp.setShippingCondition("01");
            bp.setDeliveringPlant(1000L);
            bp.setMaxPartDeliv(5);
            bp.setIncoterms("EXW");
            bp.setIncotermsLocation("Factory");
            bp.setPaymentTerms("0001");
            bp.setAcctAssignment("01");
            bp.setOutputTax(1);

            // 插入数据库
            int result = businessPartnerMapper.insertBusinessPartner(bp);
            
            if (result > 0) {
                Map<String, Object> data = new HashMap<>();
                data.put("customerId", bp.getCustomerId());
                data.put("name", bp.getName());
                
                log.info("业务伙伴创建成功: {}", bp.getCustomerId());
                return BpResponse.success(data, "Business partner created successfully.");
            } else {
                return BpResponse.error("CREATE_FAILED", "Failed to create business partner");
            }

        } catch (Exception e) {
            log.error("业务伙伴创建异常: {}", e.getMessage(), e);
            return BpResponse.error("INTERNAL_ERROR", "Internal server error occurred");
        }
    }

    @Override
    public BpResponse createGroupBusinessPartner(GroupCreateRequest request) {
        try {
            // 参数验证
            if (request.getTest() == null) {
                return BpResponse.error("INVALID_INPUT", "Group data is required");
            }

            GroupCreateRequest.Test test = request.getTest();
            
            // 验证必填字段
            if (test.getName() == null || !StringUtils.hasText(test.getName().getName())) {
                return BpResponse.error("INVALID_INPUT", "Group name is required");
            }

            if (test.getAddress() == null) {
                return BpResponse.error("INVALID_INPUT", "Address information is required");
            }

            // 构建业务伙伴对象（组类型）
            BusinessPartner bp = new BusinessPartner();
            bp.setTitle("GR"); // 组类型标识
            bp.setName(test.getName().getName());
            bp.setStreet(test.getAddress().getStreet());
            bp.setCity(test.getAddress().getCity());
            bp.setPostalCode(test.getAddress().getPostalCode());
            bp.setCountry(test.getAddress().getCountry());
            
            // 设置组特有的默认值
            bp.setLanguage("EN");
            bp.setRegion("01");
            bp.setCompanyCode("1000");
            bp.setReconciliationAccount("140000");
            bp.setSortKey("002"); // 组类型使用不同的排序键
            bp.setSalesOrg("1000");
            bp.setChannel(10);
            bp.setDivision("01");
            bp.setCurrency("USD");
            bp.setSalesDistrict("000001");
            bp.setPriceGroup("02"); // 组价格组
            bp.setCustomerGroup("02"); // 组客户组
            bp.setDeliveryPriority("02");
            bp.setShippingCondition("01");
            bp.setDeliveringPlant(1000L);
            bp.setMaxPartDeliv(5);
            bp.setIncoterms("EXW");
            bp.setIncotermsLocation("Factory");
            bp.setPaymentTerms("0001");
            bp.setAcctAssignment("01");
            bp.setOutputTax(1);

            // 插入数据库
            int result = businessPartnerMapper.insertBusinessPartner(bp);
            
            if (result > 0) {
                Map<String, Object> data = new HashMap<>();
                data.put("customerId", bp.getCustomerId());
                data.put("name", bp.getName());
                data.put("type", "GROUP");
                
                log.info("组业务伙伴创建成功: {}", bp.getCustomerId());
                return BpResponse.success(data, "Group business partner created successfully.");
            } else {
                return BpResponse.error("CREATE_FAILED", "Failed to create group business partner");
            }

        } catch (Exception e) {
            log.error("组业务伙伴创建异常: {}", e.getMessage(), e);
            return BpResponse.error("INTERNAL_ERROR", "Internal server error occurred");
        }
    }
}
