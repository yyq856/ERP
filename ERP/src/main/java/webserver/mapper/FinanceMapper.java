package webserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import webserver.pojo.SearchOpenItemsRequest;

import java.util.List;
import java.util.Map;

@Mapper
public interface FinanceMapper {
    
    /**
     * 搜索未清项
     * @param request 搜索请求参数
     * @return 未清项列表
     */
    List<Map<String, Object>> searchOpenItems(SearchOpenItemsRequest request);

    /**
     * 根据客户ID查询未清算的账单
     * @param accountId 客户ID
     * @return 未清算的账单列表
     */
    List<Map<String, Object>> getUnclearBillsByAccountId(@Param("accountId") String accountId);

    /**
     * 更新账单状态为CLEAR
     * @param billId 账单ID
     * @return 更新记录数
     */
    int updateBillStatusToClear(@Param("billId") String billId);

    /**
     * 更新账单状态为UNCLEAR
     * @param billId 账单ID
     * @return 更新记录数
     */
    int updateBillStatusToUnclear(@Param("billId") String billId);
    
    /**
     * 获取账单详细信息用于创建付款记录
     * @param billId 账单ID
     * @return 账单信息
     */
    Map<String, Object> getBillInfoForPayment(@Param("billId") String billId);
    
    /**
     * 插入付款记录
     * @param payment 付款信息
     * @return 插入记录数
     */
    int insertPayment(Map<String, Object> payment);

    /**
     * 调试方法：获取所有账单信息
     * @return 所有账单列表
     */
    List<Map<String, Object>> getAllBillsForDebug();

}
