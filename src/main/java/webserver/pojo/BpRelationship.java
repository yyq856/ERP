package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BpRelationship {
    private Long relationId;
    private String relCategory;
    private Long bp1;
    private Long bp2;
    private Integer management;
    private String department;
    private String function;
    private LocalDate validFrom;
    private LocalDate validTo;
    
    // 扩展字段，用于存储动态数据（JSON格式）
    private String generalDataJson;
}
