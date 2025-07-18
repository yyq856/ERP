package webserver.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BpRelationshipEditRequest {
    private BpRelationshipData bpRelationshipData;
    private Map<String, Object> generalData;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BpRelationshipData {
        private Meta meta;
        private BasicInfo basicInfo;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Meta {
            private String id;
        }
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class BasicInfo {
            private BpRelationshipRegisterRequest.Relation relation;
            @JsonProperty("default")
            private BpRelationshipRegisterRequest.DefaultInfo defaultInfo;
        }
    }
}
