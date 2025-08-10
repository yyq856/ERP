package webserver.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

/**
 * DateUtil 测试类
 */
public class DateUtilTest {

    @Test
    public void testParseDate_SlashFormat() throws Exception {
        // 测试前端格式 yyyy/MM/dd
        String dateStr = "2025/08/14";
        LocalDate result = DateUtil.parseDate(dateStr);
        
        assertEquals(LocalDate.of(2025, 8, 14), result);
    }

    @Test
    public void testParseDate_DashFormat() throws Exception {
        // 测试数据库格式 yyyy-MM-dd
        String dateStr = "2025-08-14";
        LocalDate result = DateUtil.parseDate(dateStr);
        
        assertEquals(LocalDate.of(2025, 8, 14), result);
    }

    @Test
    public void testParseDate_NullInput() {
        // 测试空输入
        assertThrows(Exception.class, () -> {
            DateUtil.parseDate(null);
        });
    }

    @Test
    public void testParseDate_EmptyInput() {
        // 测试空字符串
        assertThrows(Exception.class, () -> {
            DateUtil.parseDate("");
        });
    }

    @Test
    public void testParseDate_InvalidFormat() {
        // 测试无效格式
        assertThrows(Exception.class, () -> {
            DateUtil.parseDate("20250814");
        });
    }

    @Test
    public void testParseDateSafely_ValidInput() {
        // 测试安全解析 - 有效输入
        String dateStr = "2025/08/14";
        LocalDate result = DateUtil.parseDateSafely(dateStr);
        
        assertEquals(LocalDate.of(2025, 8, 14), result);
    }

    @Test
    public void testParseDateSafely_InvalidInput() {
        // 测试安全解析 - 无效输入
        String dateStr = "invalid";
        LocalDate result = DateUtil.parseDateSafely(dateStr);
        
        assertNull(result);
    }
}