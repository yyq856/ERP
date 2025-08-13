package webserver.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    
    // Customer 类型的扩展字段
    private String customerCode;
    private String customerName;
    private String contactPerson;
    
    // ContactPerson 和 test 类型的扩展字段
    private String testField;
    private String description;
    
    // 通用扩展字段
    private String extendedData; // JSON格式存储动态字段
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
