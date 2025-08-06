package webserver.pojo;

import lombok.Data;

@Data
public class OutboundDeliveryItemDTO {
    private String item;
    private String material;
    private String deliveryQuantity;
    private String deliveryQuantityUnit;
    private String pickingQuantity;
    private String pickingQuantityUnit;
    private String pickingStatus;
    private String confirmationStatus;
    private String salesOrder;
    private String itemType;
    private String originalDelivertyQuantity;
    private String conversionRate;
    private String baseUnitDeliveryQuantity;
    private String grossWeight;
    private String netWeight;
    private String volume;
    private String plant;
    private String storageLocation;
    private String storageLocationDescription;
    private String storageBin;
    private String materialAvailability;
}
