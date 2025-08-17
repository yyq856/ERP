package webserver.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import java.io.IOException;
import java.util.List;

@Data
public class QuotationDetailsResponseDTO {
    private Meta meta;
    private BasicInfo basicInfo;
    private ItemOverview itemOverview;

    /**
     * 自定义反序列化器，用于处理带逗号的数字字符串
     */
    public static class CommaFloatDeserializer extends JsonDeserializer<Float> {
        @Override
        public Float deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            if (value == null || value.trim().isEmpty()) {
                return 0.0f;
            }
            try {
                // 移除千分位分隔符（逗号）
                String cleanValue = value.replace(",", "");
                return Float.parseFloat(cleanValue);
            } catch (NumberFormatException e) {
                throw new IOException("无法解析数字: " + value, e);
            }
        }
    }

    @Data
    public static class Meta {
        private String id;
    }

    @Data
    public static class BasicInfo {
        private String inquiry;              // 询价单号
        private String quotation;            // 报价单号
        private String soldToParty;          // 售达方
        private String shipToParty;          // 送达方
        private String customerReference;    // 客户参考
        @JsonDeserialize(using = CommaFloatDeserializer.class)
        private float netValue;              // 净值
        private String netValueUnit;         // 净值单位
        private String customerReferenceDate;// 客户参考日期
    }

    @Data
    public static class ItemOverview {
        private String validFrom;
        private String validTo;
        private String reqDelivDate;
        private String expectedOralVal;
        private String expectedOralValUnit;
        private List<QuotationItemDTO> items;
    }
}
