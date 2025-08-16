package webserver.service;

import java.math.BigDecimal;

/**
 * 客户余额服务接口
 */
public interface CustomerBalanceService {

    /**
     * 获取客户余额
     * @param customerId 客户ID
     * @param companyCode 公司代码
     * @param currency 货币
     * @return 余额
     */
    BigDecimal getCustomerBalance(String customerId, String companyCode, String currency);

    /**
     * 更新客户余额
     * @param customerId 客户ID
     * @param companyCode 公司代码
     * @param currency 货币
     * @param amount 变更金额（正数增加，负数减少）
     * @return 操作结果
     */
    boolean updateCustomerBalance(String customerId, String companyCode, String currency, BigDecimal amount);

    /**
     * 检查余额是否足够
     * @param customerId 客户ID
     * @param companyCode 公司代码
     * @param currency 货币
     * @param requiredAmount 需要的金额
     * @return 是否足够
     */
    boolean hasEnoughBalance(String customerId, String companyCode, String currency, BigDecimal requiredAmount);

    /**
     * 获取公司代码下所有客户的指定货币余额总和
     * @param companyCode 公司代码
     * @param currency 货币
     * @return 余额总和
     */
    BigDecimal getTotalBalanceByCompanyAndCurrency(String companyCode, String currency);

    /**
     * 直接设置客户余额（而非累加）
     * @param customerId 客户ID
     * @param companyCode 公司代码
     * @param currency 货币
     * @param balance 新的余额值
     * @return 操作结果
     */
    boolean setCustomerBalance(String customerId, String companyCode, String currency, BigDecimal balance);
}
