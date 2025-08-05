package webserver.pojo;

import lombok.Data;

import java.util.List;

@Data
public class ValidateItemsResponse {
    private ValidateItemsResult result;
    private List<OutboundDeliveryItemDTO> breakdowns;

    @Data
    public static class ValidateItemsResult {
        private int allDataLegal; // 1 表示合法，0 表示有错误
        private List<Integer> badRecordIndices;
    }
}
