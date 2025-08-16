package webserver.event;

/**
 * 销售订单明细更新事件
 * 用于在销售订单明细更新后触发金额重新计算
 */
public class SalesOrderItemsUpdatedEvent {
    
    private final Long salesOrderId;
    
    public SalesOrderItemsUpdatedEvent(Long salesOrderId) {
        this.salesOrderId = salesOrderId;
    }
    
    public Long getSalesOrderId() {
        return salesOrderId;
    }
    
    @Override
    public String toString() {
        return "SalesOrderItemsUpdatedEvent{" +
                "salesOrderId=" + salesOrderId +
                '}';
    }
}
