package webserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webserver.mapper.StockMapper;
import webserver.pojo.MaterialInfoResponse;
import webserver.pojo.SearchStockResponseData;
import webserver.pojo.StockContentItem;
import webserver.service.StockService;

import java.util.*;

@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private StockMapper stockMapper;

    @Override
    public List<String> getStockStages() {
        return stockMapper.selectStockStages();
    }

    @Override
    public List<String> getStockLevels() {
        return stockMapper.selectStockLevels();
    }

    @Override
    public MaterialInfoResponse getMaterialInfo(String materialId) {
        return stockMapper.selectMaterialInfoById(materialId);
    }

    @Override
    public SearchStockResponseData searchStock(String materialId) {
        // 1. 获取库存阶段数和阶段名称（也可以缓存或调用另接口）
        List<String> stages = stockMapper.getStockStages(); // ["stage name 0", "stage name 1", "stage name 2"]
        int stageCount = stages.size();

        // 2. 查询数据库，获取该物料所有层级、阶段的库存数据
        // 返回 List<Map> 结构，每条记录包含 level_name, depth, stage_id, qty_on_hand
        List<Map<String, Object>> rawData = stockMapper.selectStockByMaterial(materialId);

        // 3. 整理数据，构造最终返回格式
        // 结构：Map<level, Map<stage_k, qty>>, 另存 depth
        Map<String, Map<String, String>> levelStageQtyMap = new LinkedHashMap<>();
        Map<String, Integer> levelDepthMap = new HashMap<>();

        for (Map<String, Object> row : rawData) {
            String level = (String) row.get("level_name");
            Integer depth = (Integer) row.get("depth");
            Integer stageId = (Integer) row.get("stage_id");
            Float qty = (Float) row.get("qty_on_hand");

            levelDepthMap.put(level, depth);

            // stage字段按stage0、stage1编号
            String stageKey = "stage" + (stageId - 1);

            levelStageQtyMap.putIfAbsent(level, new HashMap<>());
            levelStageQtyMap.get(level).put(stageKey, String.valueOf(qty.intValue()));
        }

        // 4. 构造返回列表，确保每个stage字段都有，即使为0
        List<StockContentItem> content = new ArrayList<>();
        for (String level : levelStageQtyMap.keySet()) {
            Map<String, String> stageQty = levelStageQtyMap.get(level);

            // 补全缺失stage，值为 "0"
            for (int i = 0; i < stageCount; i++) {
                String key = "stage" + i;
                stageQty.putIfAbsent(key, "0");
            }

            Map<String, String> data = new LinkedHashMap<>();
            data.put("level", level);
            data.putAll(stageQty);

            int depth = levelDepthMap.getOrDefault(level, 0);
            content.add(new StockContentItem(data, depth));
        }

        return new SearchStockResponseData(content);
    }
}
