package webserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webserver.mapper.SalesOrderCalculationMapper;
import webserver.service.SalesOrderCalculationService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class SalesOrderCalculationServiceImpl implements SalesOrderCalculationService {

    @Autowired
    private SalesOrderCalculationMapper salesOrderCalculationMapper;

    @Override
    public boolean recalculateAndUpdateSalesOrderAmounts(Long soId) {
        try {
            // 1. 计算金额
            SalesOrderAmountResult result = calculateSalesOrderAmounts(soId);
            
            if (result == null) {
                System.err.println("无法计算销售订单 " + soId + " 的金额");
                return false;
            }

            // 2. 更新数据库
            int updatedRows = salesOrderCalculationMapper.updateSalesOrderAmounts(
                soId, 
                result.getNetValue(), 
                result.getTaxValue(), 
                result.getGrossValue(), 
                result.getCurrency()
            );

            if (updatedRows > 0) {
                System.out.println(String.format("销售订单 %d 金额更新成功: %s", soId, result));
                return true;
            } else {
                System.err.println("销售订单 " + soId + " 更新失败，没有影响任何行");
                return false;
            }
        } catch (Exception e) {
            System.err.println("更新销售订单 " + soId + " 金额时出错: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int batchUpdateAllSalesOrderAmounts() {
        try {
            List<Long> soIds = salesOrderCalculationMapper.getAllSalesOrderIds();
            int successCount = 0;
            
            System.out.println("开始批量更新 " + soIds.size() + " 个销售订单的金额");
            
            for (Long soId : soIds) {
                if (recalculateAndUpdateSalesOrderAmounts(soId)) {
                    successCount++;
                }
            }
            
            System.out.println(String.format("批量更新完成，成功更新 %d/%d 个销售订单", successCount, soIds.size()));
            return successCount;
        } catch (Exception e) {
            System.err.println("批量更新销售订单金额时出错: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public SalesOrderAmountResult calculateSalesOrderAmounts(Long soId) {
        try {
            Map<String, Object> calculation = salesOrderCalculationMapper.calculateSalesOrderAmounts(soId);
            
            if (calculation == null || calculation.isEmpty()) {
                System.err.println("销售订单 " + soId + " 不存在或没有数据");
                return null;
            }

            // 从计算结果中提取数据
            BigDecimal netValue = getBigDecimalFromMap(calculation, "calculatedNetValue");
            BigDecimal taxValue = getBigDecimalFromMap(calculation, "calculatedTaxValue");
            BigDecimal grossValue = getBigDecimalFromMap(calculation, "calculatedGrossValue");
            String currency = (String) calculation.get("currency");
            
            // 如果没有明细，使用默认值
            if (netValue == null) netValue = BigDecimal.ZERO;
            if (taxValue == null) taxValue = BigDecimal.ZERO;
            if (grossValue == null) grossValue = BigDecimal.ZERO;
            if (currency == null || currency.isEmpty()) currency = "USD";

            // 如果 grossValue 为 0，尝试用 netValue + taxValue 计算
            if (grossValue.compareTo(BigDecimal.ZERO) == 0 && netValue.compareTo(BigDecimal.ZERO) > 0) {
                grossValue = netValue.add(taxValue);
            }

            return new SalesOrderAmountResult(netValue, taxValue, grossValue, currency);
        } catch (Exception e) {
            System.err.println("计算销售订单 " + soId + " 金额时出错: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从 Map 中安全获取 BigDecimal 值
     */
    private BigDecimal getBigDecimalFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            System.err.println("无法转换值 " + value + " 为 BigDecimal: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @Override
    public Map<String, Object> getSalesOrderInfo(Long soId) {
        return salesOrderCalculationMapper.getSalesOrderInfo(soId);
    }
}
