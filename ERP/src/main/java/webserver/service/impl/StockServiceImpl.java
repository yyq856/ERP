package webserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webserver.common.Response;
import webserver.mapper.StockMapper;
import webserver.pojo.*;
import webserver.service.StockService;

import java.util.*;

@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private StockMapper stockMapper;

    @Override
    public MaterialInfoResponse getMaterialInfo(String materialId) {
        return stockMapper.selectMaterialInfoById(materialId);
    }

    @Override
    public Response<SearchStockResponseData> searchStock(SearchStockRequest request) {
        String matId = request.getId();
        List<StockRecord> records = stockMapper.selectStockByMatId(matId);

        double totalOnHand = 0, totalCommitted = 0;

        Map<Long, Map<Long, StockRecord>> hierarchy = new LinkedHashMap<>();

        for (StockRecord r : records) {
            totalOnHand += r.getQtyOnHand();
            totalCommitted += r.getQtyCommitted();

            hierarchy.computeIfAbsent(r.getBpId(), k -> new LinkedHashMap<>())
                    .put(r.getPlantId(), r);
        }

        List<StockLevelNode> result = new ArrayList<>();

        // 根节点，总计
        result.add(createNode("Full", totalOnHand, totalCommitted, 0));

        for (Map.Entry<Long, Map<Long, StockRecord>> bpEntry : hierarchy.entrySet()) {
            Long bpId = bpEntry.getKey();
            Map<Long, StockRecord> plantMap = bpEntry.getValue();

            String bpLevel = bpId.toString();
            result.add(createNode(bpLevel, sumQtyOnHand(plantMap), sumQtyCommitted(plantMap), 1));

            for (Map.Entry<Long, StockRecord> plantEntry : plantMap.entrySet()) {
                Long plantId = plantEntry.getKey();
                StockRecord rec = plantEntry.getValue();

                String plantName = stockMapper.selectPlantNameById(plantId);
                result.add(createNode(plantName, rec.getQtyOnHand(), rec.getQtyCommitted(), 2));
            }
        }

        SearchStockResponseData responseData = new SearchStockResponseData();
        responseData.setContent(result);

        return Response.success(responseData);
    }

    private StockLevelNode createNode(String level, double stage0Val, double stage1Val, int depth) {
        StockLevelData data = new StockLevelData();
        data.setLevel(level);
        data.setStage0(stage0Val == 0 ? "" : String.valueOf((int)stage0Val));
        data.setStage1(stage1Val == 0 ? "" : String.valueOf((int)stage1Val));

        StockLevelNode node = new StockLevelNode();
        node.setData(data);
        node.setDepth(depth);
        return node;
    }

    private double sumQtyOnHand(Map<Long, StockRecord> plantMap) {
        return plantMap.values().stream().mapToDouble(StockRecord::getQtyOnHand).sum();
    }

    private double sumQtyCommitted(Map<Long, StockRecord> plantMap) {
        return plantMap.values().stream().mapToDouble(StockRecord::getQtyCommitted).sum();
    }
}
