package webserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import webserver.pojo.MaterialInfoResponse;

import java.util.List;
import java.util.Map;

@Mapper
public interface StockMapper {
    List<String> selectStockStages();
    List<String> selectStockLevels();
    MaterialInfoResponse selectMaterialInfoById(@Param("matId") String matId);
    List<String> getStockStages();
    List<Map<String, Object>> selectStockByMaterial(@Param("materialId") String materialId);

}
