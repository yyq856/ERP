package webserver.pojo;

import lombok.Data;

@Data
public class MaterialInfoResponse {
    private String name;          // 对应 mat_desc
    private String materialType;  // 对应 division
    private String unit;          // 对应 base_uom
}
