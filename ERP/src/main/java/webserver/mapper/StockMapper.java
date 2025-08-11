package webserver.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import webserver.pojo.MaterialInfoResponse;
import webserver.pojo.StockRecord;

import java.util.List;
import java.util.Map;

@Mapper
public interface StockMapper {
    MaterialInfoResponse selectMaterialInfoById(@Param("matId") String matId);

    @Select("""
                SELECT
                    s.bp_id AS bpId,
                    s.plant_id AS plantId,
                    SUM(s.qty_on_hand) AS qtyOnHand,
                    SUM(s.qty_committed) AS qtyCommitted
                FROM erp_stock s
                WHERE s.mat_id = #{matId}
                GROUP BY s.bp_id, s.plant_id
            """)
    List<StockRecord> selectStockByMatId(@Param("matId") String matId);


    @Select("""
        SELECT plant_name FROM erp_plant_name WHERE plant_id = #{plantId} LIMIT 1
    """)
    String selectPlantNameById(@Param("plantId") Long plantId);
}
