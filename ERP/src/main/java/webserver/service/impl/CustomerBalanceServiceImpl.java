package webserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webserver.mapper.CustomerBalanceMapper;
import webserver.service.CustomerBalanceService;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 客户余额服务实现（数据库版本）
 */
@Service
public class CustomerBalanceServiceImpl implements CustomerBalanceService {

    @Autowired
    private CustomerBalanceMapper customerBalanceMapper;

    // 内存存储客户余额：key = "customerId:companyCode:currency", value = balance（作为缓存）
    private final Map<String, BigDecimal> balanceMap = new ConcurrentHashMap<>();

    @Override
    public BigDecimal getCustomerBalance(String customerId, String companyCode, String currency) {
        String key = buildKey(customerId, companyCode, currency);

        // 先从缓存中查找
        BigDecimal cachedBalance = balanceMap.get(key);
        if (cachedBalance != null) {
            return cachedBalance;
        }

        // 从数据库查找
        try {
            Long customerIdLong = Long.parseLong(customerId);
            BigDecimal dbBalance = customerBalanceMapper.getOrCreateCustomerBalance(customerIdLong, companyCode, currency);
            if (dbBalance != null) {
                // 更新缓存
                balanceMap.put(key, dbBalance);
                return dbBalance;
            }
        } catch (Exception e) {
            System.err.println("从数据库获取余额失败: " + e.getMessage());
        }

        return BigDecimal.ZERO;
    }

    @Override
    public boolean updateCustomerBalance(String customerId, String companyCode, String currency, BigDecimal amount) {
        String key = buildKey(customerId, companyCode, currency);

        synchronized (this) {
            // 先获取当前余额（可能从数据库读取）
            BigDecimal currentBalance = getCustomerBalance(customerId, companyCode, currency);
            // BigDecimal newBalance = currentBalance.add(amount);
            BigDecimal newBalance = currentBalance.add(amount.negate());

            // 防止余额变为负数
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                return false;
            }

            try {
                // 更新数据库
                Long customerIdLong = Long.parseLong(customerId);
                int updatedRows = customerBalanceMapper.updateCustomerBalance(customerIdLong, companyCode, currency, amount);

                if (updatedRows > 0) {
                    // 更新缓存
                    balanceMap.put(key, newBalance);
                    System.out.println(String.format("客户余额更新: %s, 变更: %s, 新余额: %s",
                        key, amount, newBalance));
                    return true;
                } else {
                    System.err.println("数据库更新失败，影响行数为0");
                    return false;
                }
            } catch (Exception e) {
                System.err.println("更新客户余额失败: " + e.getMessage());
                return false;
            }
        }
    }

    @Override
    public boolean hasEnoughBalance(String customerId, String companyCode, String currency, BigDecimal requiredAmount) {
        BigDecimal currentBalance = getCustomerBalance(customerId, companyCode, currency);
        return currentBalance.compareTo(requiredAmount) >= 0;
    }

    /**
     * 构建存储键
     */
    private String buildKey(String customerId, String companyCode, String currency) {
        return String.format("%s:%s:%s", customerId, companyCode, currency);
    }

    @Override
    public BigDecimal getTotalBalanceByCompanyAndCurrency(String companyCode, String currency) {
        try {
            // 从数据库获取总余额
            BigDecimal dbTotal = customerBalanceMapper.getTotalBalanceByCompanyAndCurrency(companyCode, currency);
            if (dbTotal != null) {
                System.out.println(String.format("公司 %s 的 %s 货币总余额: %s",
                    companyCode, currency, dbTotal));
                return dbTotal;
            }
        } catch (Exception e) {
            System.err.println("从数据库获取总余额失败: " + e.getMessage());
        }

        // 回退到内存计算
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : balanceMap.entrySet()) {
            String key = entry.getKey();
            String[] parts = key.split(":");
            if (parts.length == 3) {
                String keyCompanyCode = parts[1];
                String keyCurrency = parts[2];
                if (companyCode.equals(keyCompanyCode) && currency.equals(keyCurrency)) {
                    total = total.add(entry.getValue());
                }
            }
        }

        System.out.println(String.format("公司 %s 的 %s 货币总余额: %s",
            companyCode, currency, total));
        return total;
    }

    /**
     * 调试方法：获取所有余额
     */
    public Map<String, BigDecimal> getAllBalances() {
        try {
            // 从数据库获取所有余额
            Map<String, BigDecimal> dbBalances = customerBalanceMapper.getAllBalances();
            if (dbBalances != null && !dbBalances.isEmpty()) {
                return dbBalances;
            }
        } catch (Exception e) {
            System.err.println("从数据库获取所有余额失败: " + e.getMessage());
        }
        // 回退到内存版本
        return new ConcurrentHashMap<>(balanceMap);
    }

    @Override
    public boolean setCustomerBalance(String customerId, String companyCode, String currency, BigDecimal balance) {
        try {
            Long customerIdLong = Long.parseLong(customerId);
            int result = customerBalanceMapper.setCustomerBalance(customerIdLong, companyCode, currency, balance);

            if (result > 0) {
                // 同步更新内存缓存
                String key = customerId + ":" + companyCode + ":" + currency;
                balanceMap.put(key, balance);
                System.out.println(String.format("客户余额直接设置: %s, 新余额: %s", key, balance));
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("设置客户余额失败: " + e.getMessage());
            return false;
        }
    }
}
