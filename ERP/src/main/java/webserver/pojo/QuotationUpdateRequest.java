package webserver.pojo;

import lombok.Data;

/**
 * 报价单更新请求类
 * 用于接收前端发送的 {"quotation": {...}} 格式数据
 */
@Data
public class QuotationUpdateRequest {
    private QuotationDetailsResponseDTO quotation; // 前端发送的是 "quotation" 字段
}
