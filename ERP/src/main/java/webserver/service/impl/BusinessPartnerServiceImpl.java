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

    @Override
    public BpMaintainSearchResponse searchBusinessPartnersForMaintain(BpMaintainSearchRequest request) {
        try {
            BpMaintainSearchResponse response = new BpMaintainSearchResponse();

            // 参数验证
            if (request.getQuery() == null || !StringUtils.hasText(request.getQuery().getCustomerId())) {
                response.setSuccess(false);
                response.setMessage("Customer ID is required");
                return response;
            }

            // 查询业务伙伴列表
            java.util.List<BpMaintainSearchResponse.BpSearchItem> searchResults =
                businessPartnerMapper.searchBusinessPartnersForMaintain(request.getQuery().getCustomerId());

            response.setSuccess(true);
            response.setMessage("查询成功");
            response.setData(searchResults);

            log.info("BP维护页面搜索完成，找到 {} 条记录", searchResults.size());
            return response;

        } catch (Exception e) {
            log.error("BP维护页面搜索异常: {}", e.getMessage(), e);
            BpMaintainSearchResponse response = new BpMaintainSearchResponse();
            response.setSuccess(false);
            response.setMessage("Internal server error occurred");
            return response;
        }
    }

    @Override
    public BpMaintainDetailResponse getBusinessPartnerDetail(String customerId) {
        try {
            BpMaintainDetailResponse response = new BpMaintainDetailResponse();

            // 参数验证
            if (!StringUtils.hasText(customerId)) {
                response.setSuccess(false);
                response.setMessage("Customer ID is required");
                return response;
            }

            // 查询业务伙伴详情
            BusinessPartner bp = businessPartnerMapper.findBusinessPartnerDetailById(customerId);

            if (bp == null) {
                response.setSuccess(false);
                response.setMessage("Business partner not found");
                return response;
            }

            // 构建响应数据
            BpMaintainDetailResponse.BpDetailData data = new BpMaintainDetailResponse.BpDetailData();

            // BP ID和角色部分
            BpMaintainDetailResponse.BpIdAndRoleSection bpIdSection = new BpMaintainDetailResponse.BpIdAndRoleSection();
            bpIdSection.setCustomerId(bp.getCustomerId().toString());
            bpIdSection.setBpRole("Customer"); // 默认角色，可根据实际需求调整
            bpIdSection.setType(bp.getBpType() != null ? bp.getBpType() : "org");
            data.setBpIdAndRoleSection(bpIdSection);

            // 名称部分
            BpMaintainDetailResponse.NameSection nameSection = new BpMaintainDetailResponse.NameSection();
            nameSection.setTitle(bp.getTitle());
            nameSection.setName(bp.getName());
            nameSection.setFirstName(bp.getFirstName());
            nameSection.setLastName(bp.getLastName());
            data.setName(nameSection);

            // 搜索词部分
            BpMaintainDetailResponse.SearchTermsSection searchTermsSection = new BpMaintainDetailResponse.SearchTermsSection();
            searchTermsSection.setSearchTerm(bp.getSearchTerm());
            data.setSearchTerms(searchTermsSection);

            // 地址部分
            BpMaintainDetailResponse.AddressSection addressSection = new BpMaintainDetailResponse.AddressSection();
            addressSection.setCountry(bp.getCountry());
            addressSection.setStreet(bp.getStreet());
            addressSection.setPostalCode(bp.getPostalCode());
            addressSection.setCity(bp.getCity());
            data.setAddress(addressSection);

            response.setSuccess(true);
            response.setMessage("查询成功");
            response.setData(data);

            log.info("获取BP详情成功: {}", customerId);
            return response;

        } catch (Exception e) {
            log.error("获取BP详情异常: {}", e.getMessage(), e);
            BpMaintainDetailResponse response = new BpMaintainDetailResponse();
            response.setSuccess(false);
            response.setMessage("Internal server error occurred");
            return response;
        }
    }

    @Override
    public BpMaintainEditResponse editBusinessPartner(BpMaintainEditRequest request) {
        try {
            log.info("开始处理编辑业务伙伴请求");
            BpMaintainEditResponse response = new BpMaintainEditResponse();

            // 参数验证
            if (request.getBpIdAndRoleSection() == null) {
                log.warn("BP ID and role section is null");
                response.setSuccess(false);
                response.setMessage("BP ID and role section is required");
                return response;
            }

            log.info("参数验证通过，开始构建BusinessPartner对象");

            // 构建业务伙伴对象
            BusinessPartner bp = new BusinessPartner();

            // 判断是创建还是更新操作
            boolean isUpdate = false;
            Long existingCustomerId = null;

            if (request.getBpIdAndRoleSection() != null) {
                String customerIdStr = request.getBpIdAndRoleSection().getCustomerId();
                if (StringUtils.hasText(customerIdStr)) {
                    try {
                        // 尝试解析为数字，如果成功且大于0，则认为是更新操作
                        existingCustomerId = Long.parseLong(customerIdStr);
                        if (existingCustomerId > 0) {
                            // 检查该ID是否在数据库中存在
                            BusinessPartner existing = businessPartnerMapper.findBusinessPartnerDetailById(existingCustomerId.toString());
                            if (existing != null) {
                                isUpdate = true;
                                bp.setCustomerId(existingCustomerId);
                                log.info("检测到更新操作，customerId: {}", existingCustomerId);
                            } else {
                                log.info("customerId {} 在数据库中不存在，将执行新增操作", existingCustomerId);
                            }
                        }
                    } catch (NumberFormatException e) {
                        // 如果无法解析为数字，则认为是新增操作的临时标识符
                        log.info("customerId {} 不是数字格式，将执行新增操作", customerIdStr);
                    }
                }
                bp.setBpType(request.getBpIdAndRoleSection().getType());
            }

            // 设置名称信息
            if (request.getName() != null) {
                bp.setTitle(request.getName().getTitle());
                bp.setName(request.getName().getName());
                bp.setFirstName(request.getName().getFirstName());
                bp.setLastName(request.getName().getLastName());
            }

            // 设置搜索词
            if (request.getSearchTerms() != null) {
                bp.setSearchTerm(request.getSearchTerms().getSearchTerm());
            }

            // 设置地址信息
            if (request.getAddress() != null) {
                bp.setCountry(request.getAddress().getCountry());
                bp.setStreet(request.getAddress().getStreet());
                bp.setPostalCode(request.getAddress().getPostalCode());
                bp.setCity(request.getAddress().getCity());
            }

            int result;
            String resultCustomerId;

            if (isUpdate) {
                // 更新现有业务伙伴
                log.info("执行更新操作，customerId: {}", bp.getCustomerId());
                result = businessPartnerMapper.updateBusinessPartner(bp);
                resultCustomerId = bp.getCustomerId().toString();
                log.info("更新业务伙伴完成，影响行数: {}", result);
            } else {
                // 创建新业务伙伴 - 不设置customerId，让数据库自动生成
                bp.setCustomerId(null);
                log.info("执行新增操作");
                result = businessPartnerMapper.insertBusinessPartnerForMaintain(bp);
                resultCustomerId = bp.getCustomerId() != null ? bp.getCustomerId().toString() : "unknown";
                log.info("创建新业务伙伴完成，生成的customerId: {}, 影响行数: {}", resultCustomerId, result);
            }

            if (result > 0) {
                BpMaintainEditResponse.BpEditData data = new BpMaintainEditResponse.BpEditData();
                data.setCustomerId(resultCustomerId);

                response.setSuccess(true);
                response.setMessage("操作成功");
                response.setData(data);

                return response;
            } else {
                response.setSuccess(false);
                response.setMessage("操作失败");
                return response;
            }

        } catch (Exception e) {
            log.error("编辑业务伙伴异常: {}", e.getMessage(), e);
            BpMaintainEditResponse response = new BpMaintainEditResponse();
            response.setSuccess(false);
            response.setMessage("Internal server error occurred: " + e.getMessage());
            return response;
        }
    }
}
