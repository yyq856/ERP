package webserver.pojo;

import lombok.Data;
import java.util.List;

@Data
public class QuotationResponseDTO1 {
    private QuotationDetailsResponseDTO quotationData; // 外层包一层 quotation
}