package webserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Map;

@Mapper
public interface CustomerBalanceMapper {

    /**
     * 获取客户余额
     * @param customerId 客户ID
     * @param companyCode 公司代码
     * @param currency 货币
     * @return 余额信息
     */
    Map<String, Object> getCustomerBalance(@Param("customerId") Long customerId, 
                                         @Param("companyCode") String companyCode, 
                                         @Param("currency") String currency);

    /**
     * 创建客户余额记录（如果不存在）
     * @param customerId 客户ID
     * @param companyCode 公司代码
     * @param currency 货币
     * @return 影响行数
     */
    int createCustomerBalanceIfNotExists(@Param("customerId") Long customerId, 
                                       @Param("companyCode") String companyCode, 
                                       @Param("currency") String currency);

    /**
     * 更新客户余额
     * @param customerId 客户ID
     * @param companyCode 公司代码
     * @param currency 货币
     * @param amount 变更金额（正数增加，负数减少）
     * @return 影响行数
     */
    int updateCustomerBalance(@Param("customerId") Long customerId, 
                            @Param("companyCode") String companyCode, 
                            @Param("currency") String currency, 
                            @Param("amount") BigDecimal amount);

    /**
     * 获取客户余额（如果不存在则创建）
     * @param customerId 客户ID
     * @param companyCode 公司代码
     * @param currency 货币
     * @return 余额
     */
    BigDecimal getOrCreateCustomerBalance(@Param("customerId") Long customerId,
                                        @Param("companyCode") String companyCode,
                                        @Param("currency") String currency);

    /**
     * 获取指定公司和货币的总余额
     * @param companyCode 公司代码
     * @param currency 货币
     * @return 总余额
     */
    BigDecimal getTotalBalanceByCompanyAndCurrency(@Param("companyCode") String companyCode,
                                                 @Param("currency") String currency);

    /**
     * 获取所有余额记录
     * @return 余额映射表
     */
    Map<String, BigDecimal> getAllBalances();

    /**
     * 直接设置客户余额（而非累加）
     * @param customerId 客户ID
     * @param companyCode 公司代码
     * @param currency 货币
     * @param balance 新的余额值
     * @return 影响行数
     */
    int setCustomerBalance(@Param("customerId") Long customerId,
                          @Param("companyCode") String companyCode,
                          @Param("currency") String currency,
                          @Param("balance") BigDecimal balance);
}
