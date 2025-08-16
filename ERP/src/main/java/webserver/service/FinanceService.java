package webserver.service;

import webserver.pojo.SearchOpenItemsRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface FinanceService {

    /**
     * 搜索未清项
     * @param request 搜索请求参数
     * @return 搜索结果
     */
    Map<String, Object> searchOpenItems(SearchOpenItemsRequest request);

    /**
     * 根据客户ID查询未清算的账单
     * @param accountId 客户ID
     * @return 未清算的账单列表
     */
    List<Map<String, Object>> getUnclearBillsByAccountId(String accountId);

    /**
     * 处理 incoming payment，更新账单状态并创建付款记录
     * @param billId 账单ID
     * @return 操作结果
     */
    Map<String, Object> processIncomingPayment(String billId);

    /**
     * 处理 incoming payment（带支付金额），更新账单状态并创建付款记录
     * @param billId 账单ID
     * @param paymentAmount 客户实际支付金额
     * @param currency 货币
     * @return 操作结果
     */
    Map<String, Object> processIncomingPaymentWithAmount(String billId, BigDecimal paymentAmount, String currency);

    /**
     * 调试方法：获取所有账单信息
     * @return 所有账单列表
     */
    List<Map<String, Object>> getAllBillsForDebug();

    /**
     * 更新账单状态为UNCLEAR (现在实际更新为OPEN)
     * @param billId 账单ID
     * @return 操作结果
     */
    boolean updateBillStatusToUnclear(String billId);

    /**
     * 更新账单状态为OPEN
     * @param billId 账单ID
     * @return 操作结果
     */
    boolean updateBillStatusToOpen(String billId);

}
