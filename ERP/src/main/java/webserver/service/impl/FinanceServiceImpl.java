package webserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webserver.mapper.FinanceMapper;
import webserver.service.FinanceService;
import webserver.pojo.SearchOpenItemsRequest;

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
}
