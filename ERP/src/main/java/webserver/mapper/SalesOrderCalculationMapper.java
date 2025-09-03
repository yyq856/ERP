package webserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface SalesOrderCalculationMapper {

    /**
     * 计算销售订单的金额汇总
     * @param soId 销售订单ID
     * @return 金额汇总信息
     */
    Map<String, Object> calculateSalesOrderAmounts(@Param("soId") Long soId);

    /**
     * 更新销售订单头表的金额
     * @param soId 销售订单ID
     * @param netValue 净值
     * @param taxValue 税值
     * @param grossValue 总值
     * @param currency 货币
     * @return 影响行数
     */
    int updateSalesOrderAmounts(@Param("soId") Long soId,
                              @Param("netValue") BigDecimal netValue,
                              @Param("taxValue") BigDecimal taxValue,
                              @Param("grossValue") BigDecimal grossValue,
                              @Param("currency") String currency);

    /**
     * 获取所有需要更新的销售订单ID
     * @return 销售订单ID列表
     */
    List<Long> getAllSalesOrderIds();

    /**
     * 获取销售订单的基本信息
     * @param soId 销售订单ID
     * @return 销售订单信息
     */
    Map<String, Object> getSalesOrderInfo(@Param("soId") Long soId);
}
