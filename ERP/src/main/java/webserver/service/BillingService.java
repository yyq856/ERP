package webserver.service;

import webserver.pojo.BillingInitializeRequest;
import webserver.pojo.BillingGetRequest;
import webserver.pojo.BillingEditRequest;

import webserver.pojo.BillingSearchRequest;
import webserver.pojo.ItemValidationRequest.Item;

import java.util.List;
import java.util.Map;

public interface BillingService {
    
    /**
     * 初始化开票数据
     * @param request 请求参数，包含billingDate和soldToParty
     * @return 开票凭证模板数据
     */
    Map<String, Object> initializeBilling(BillingInitializeRequest request);
    
    /**
     * 获取开票凭证详情
     * @param request 请求参数，包含billingDocumentId
     * @return 开票凭证详细信息
     */
    Map<String, Object> getBilling(BillingGetRequest request);
    
    /**
     * 创建/编辑开票凭证
     * @param request 请求参数，包含开票凭证完整信息
     * @return 开票凭证详细信息
     */
    Map<String, Object> editBilling(BillingEditRequest request);
    
    /**
     * 搜索开票凭证
     * @param request 搜索条件
     * @return 开票凭证列表
     */
    Map<String, Object> searchBilling(BillingSearchRequest request);
    
    /**
     * 验证开票凭证物品
     * @param items 物品列表
     * @return 验证结果
     */
    Map<String, Object> validateBillingItems(List<Item> items);
}
