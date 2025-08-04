package webserver.pojo;

import lombok.Data;

import java.util.List;

@Data
public class OutboundDeliveryDetailDTO {
    private Meta meta;
    private String actualGIDate;
    private String plannedGIDate;
    private String actualDate;
    private String loadingDate;
    private String deliveryDate;
    private String pickingStatus;
    private String overallStatus;
    private String giStatus;
    private String shipToParty;
    private String address;
    private String grossWeight;
    private String grossWeightUnit;
    private String netWeight;
    private String netWeightUnit;
    private String volume;
    private String volumeUnit;
    private String priority;
    private String shippingPoint;
    private List<OutboundDeliveryItemDTO> items;

    @Data
    public static class Meta {
        private String id;
        private boolean posted;
        private boolean readyToPost;
    }
}
