package webserver.mapper;

import org.apache.ibatis.annotations.*;
import webserver.pojo.MaterialInfoResponse;
import webserver.pojo.StockRecord;

import java.util.List;

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

    @Select("SELECT plant_name FROM erp_plant_name WHERE plant_id = #{plantId} LIMIT 1")
    String selectPlantNameById(@Param("plantId") Long plantId);

    // 预约（承诺）库存：若存在记录则在 qty_committed 上累加
    @Update("""
        UPDATE erp_stock
        SET qty_committed = qty_committed + #{quantity}
        WHERE plant_id = #{plantId}
          AND mat_id = #{matId}
          AND bp_id = #{bpId}
          AND storage_loc = #{storageLoc}
    """)
    int reserveStock(@Param("plantId") Long plantId,
                     @Param("matId") Long matId,
                     @Param("bpId") Long bpId,
                     @Param("storageLoc") String storageLoc,
                     @Param("quantity") int quantity);

    // 若无记录，插入一条仅承诺数量的记录（在手为0）
    @Insert("""
        INSERT INTO erp_stock (plant_id, mat_id, bp_id, storage_loc, qty_on_hand, qty_committed)
        VALUES (#{plantId}, #{matId}, #{bpId}, #{storageLoc}, 0, #{quantity})
    """)
    int insertCommittedOnly(@Param("plantId") Long plantId,
                            @Param("matId") Long matId,
                            @Param("bpId") Long bpId,
                            @Param("storageLoc") String storageLoc,
                            @Param("quantity") int quantity);

    // 发货过账：扣减在手并释放承诺（不允许承诺为负数）
    @Update("""
        UPDATE erp_stock
        SET qty_on_hand = qty_on_hand - #{quantity},
            qty_committed = GREATEST(qty_committed - #{quantity}, 0)
        WHERE plant_id = #{plantId}
          AND mat_id = #{matId}
          AND bp_id = #{bpId}
          AND storage_loc = #{storageLoc}
    """)
    int issueAndRelease(@Param("plantId") Long plantId,
                        @Param("matId") Long matId,
                        @Param("bpId") Long bpId,
                        @Param("storageLoc") String storageLoc,
                        @Param("quantity") int quantity);
}
