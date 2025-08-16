package webserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webserver.mapper.FinanceMapper;
import webserver.pojo.SearchOpenItemsRequest;
import webserver.service.FinanceService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinanceServiceImpl implements FinanceService {
    
    @Autowired
    private FinanceMapper financeMapper;
    
    @Override
    public Map<String, Object> searchOpenItems(SearchOpenItemsRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取未清项列表
        List<Map<String, Object>> openItems = financeMapper.searchOpenItems(request);
        
        // 计算总余额
        double totalBalance = 0;
        String currency = "USD"; // 默认货币
        
        if (!openItems.isEmpty()) {
            for (Map<String, Object> item : openItems) {
                totalBalance += (Double) item.get("amount");
            }
            // 获取第一个项目的货币作为总余额货币
            currency = (String) openItems.get(0).get("amountUnit");
        }
        
        result.put("balance", String.format("%.2f", totalBalance));
        result.put("balanceUnit", currency);
        result.put("openItems", openItems);
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getUnclearBillsByAccountId(String accountId) {
        return financeMapper.getUnclearBillsByAccountId(accountId);
    }

    @Override
    public Map<String, Object> processIncomingPayment(String billId) {
        // 开始事务处理
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 获取账单信息用于创建付款记录
            Map<String, Object> billInfo = financeMapper.getBillInfoForPayment(billId);
            if (billInfo == null || billInfo.isEmpty()) {
                throw new RuntimeException("未找到账单信息: " + billId);
            }
            
            // 2. 更新账单状态为CLEAR
            int updatedRows = financeMapper.updateBillStatusToClear(billId);
            if (updatedRows <= 0) {
                throw new RuntimeException("更新账单状态失败: " + billId);
            }
            
            // 3. 插入付款记录
            int insertedRows = financeMapper.insertPayment(billInfo);
            if (insertedRows <= 0) {
                throw new RuntimeException("插入付款记录失败");
            }
            
            // 4. 构建返回结果
            result.put("billId", billInfo.get("billId"));
            result.put("customerId", billInfo.get("customerId"));
            result.put("amount", billInfo.get("amount"));
            result.put("currency", billInfo.get("currency"));
            result.put("postingDate", billInfo.get("postingDate"));
            result.put("status", "CLEAR");
            result.put("message", "付款处理成功");
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("处理incoming payment失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getAllBillsForDebug() {
        return financeMapper.getAllBillsForDebug();
    }

    @Override
    public boolean updateBillStatusToUnclear(String billId) {
        try {
            int updated = financeMapper.updateBillStatusToUnclear(billId);
            return updated > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新账单状态为UNCLEAR失败: " + e.getMessage(), e);
        }
    }

}
