package webserver.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BpRelationshipRegisterRequest {
    private Relation relation;
    @JsonProperty("default")
    private DefaultInfo defaultInfo;
    
    // 前端传来的额外数据，根据relationShipCategory不同而不同
    private Map<String, Object> generalData;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Relation {
        private String relationShipCategory;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefaultInfo {
        private String businessPartner1;
        private String businessPartner2;
        private String validFrom;
        private String validTo;
    }
}
