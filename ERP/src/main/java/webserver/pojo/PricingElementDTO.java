package webserver.pojo;

import lombok.Data;

@Data
public class PricingElementDTO {
    private String cnty;                    // 国家代码/条件类型
    private String name;                    // 条件名称 (例如: "Base Price", "Tax", "Discount")
    private String amount;                  // 金额
    private String city;                    // 城市/货币标识
    private String per;                     // 每 (例如: "1")
    private String uom;                     // 计量单位 (例如: "EA", "KG")
    private String conditionValue;          // 条件值
    private String curr;                    // 货币代码 (例如: "USD", "EUR")
    private String status;                  // 状态 (例如: "Active", "Inactive")
    private String numC;                    // 数量条件
    private String atoMtsComponent;         // ATO/MTS组件标识
    private String oun;                     // OUn字段
    private String cconDe;                  // CConDe字段
    private String un;                      // Un字段
    private String conditionValue2;         // 条件值2
    private String cdCur;                   // CdCur字段
    private Boolean stat;                   // 统计标志
}
