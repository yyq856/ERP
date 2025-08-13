package webserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import webserver.pojo.BillingEditRequest;
import webserver.pojo.BillingSearchRequest;


import java.util.List;
import java.util.Map;

@Mapper
public interface BillingMapper {
    
    /**
     * 根据售达方获取客户信息
     * @param soldToParty 售达方
     * @return 客户信息
     */
    Map<String, Object> getCustomerBySoldToParty(@Param("soldToParty") String soldToParty);
    
    /**
     * 获取开票项目列表
     * @param billingDate 开票日期
     * @param soldToParty 售达方
     * @return 项目列表
     */
    List<Map<String, Object>> getBillingItems(@Param("billingDate") String billingDate, 
                                              @Param("soldToParty") String soldToParty);
    
    /**
     * 根据开票凭证号获取开票凭证基本信息
     * @param billingDocumentId 开票凭证号
     * @return 开票凭证基本信息
     */
    Map<String, Object> getBillingHeader(@Param("billingDocumentId") String billingDocumentId);
    
    /**
     * 根据开票凭证号获取开票凭证项目信息
     * @param billingDocumentId 开票凭证号
     * @return 开票凭证项目信息
     */
    List<Map<String, Object>> getBillingItemsById(@Param("billingDocumentId") String billingDocumentId);
    
    /**
     * 创建开票凭证
     * @param request 开票凭证信息
     */
    void createBilling(BillingEditRequest request);
    
    /**
     * 更新开票凭证
     * @param request 开票凭证信息
     */
    void updateBilling(BillingEditRequest request);
    
    /**
     * 删除开票凭证项目（更新时先删除再插入）
     * @param billId 开票凭证ID
     */
    void deleteBillingItems(@Param("billId") String billId);
    
    /**
     * 插入开票凭证项目
     * @param billId 开票凭证ID
     * @param item 项目信息
     */
    void insertBillingItem(@Param("billId") String billId, @Param("item") Map<String, Object> item);
    
    /**
     * 根据交货单ID获取客户ID
     * @param dlvId 交货单ID
     * @return 客户ID
     */
    String getCustomerIdByDeliveryId(@Param("dlvId") String dlvId);
    
    /**
     * 检查交货单是否存在
     * @param dlvId 交货单ID
     * @return 存在的数量
     */
    int checkDeliveryExists(@Param("dlvId") String dlvId);
    
    /**
     * 根据交货单ID获取销售订单ID和项目号
     * @param dlvId 交货单ID
     * @return 销售订单信息
     */
    Map<String, Object> getDeliveryInfo(@Param("dlvId") String dlvId);
    
    /**
     * 获取定价元素
     * @param soId 销售订单ID
     * @param itemNo 项目号
     * @return 定价元素列表
     */
    List<Map<String, Object>> getPricingElements(@Param("soId") Long soId, @Param("itemNo") Integer itemNo);
    
    /**
     * 搜索开票凭证
     * @param request 搜索条件
     * @return 开票凭证列表
     */
    List<Map<String, Object>> searchBilling(BillingSearchRequest request);
    
    /**
     * 根据物料ID获取物料信息
     * @param materialId 物料ID
     * @return 物料信息
     */
    Map<String, Object> getMaterialById(@Param("materialId") Long materialId);
    
    /**
     * 根据物料ID和销售订单ID获取销售项目价格信息
     * @param materialId 物料ID
     * @param soId 销售订单ID
     * @return 销售项目价格信息
     */
    Map<String, Object> getSalesItemPrice(@Param("materialId") Long materialId, @Param("soId") Long soId);
    
    /**
     * 根据交货单ID获取交货单信息
     * @param deliveryId 交货单ID
     * @return 交货单信息
     */
    Map<String, Object> getDeliveryById(@Param("deliveryId") String deliveryId);
    
    /**
     * 检查交货单是否已完成
     * @param deliveryId 交货单ID
     * @return 是否已完成
     */
    boolean isDeliveryCompleted(@Param("deliveryId") String deliveryId);

    /**
     * 根据交货单ID获取开票项目列表
     * @param deliveryId 交货单ID
     * @return 项目列表
     */
    List<Map<String, Object>> getBillingItemsByDeliveryId(@Param("deliveryId") String deliveryId);

}
