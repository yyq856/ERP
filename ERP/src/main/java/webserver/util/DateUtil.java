package webserver.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 日期工具类
 * 处理前端和数据库之间的日期格式转换
 */
public class DateUtil {
    
    /**
     * 解析日期，支持多种格式
     * @param dateStr 日期字符串，支持 "yyyy/MM/dd" 和 "yyyy-MM-dd" 格式
     * @return LocalDate对象
     * @throws Exception 如果日期格式不支持
     */
    public static LocalDate parseDate(String dateStr) throws Exception {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("日期字符串不能为空");
        }
        
        dateStr = dateStr.trim();
        
        try {
            // 尝试解析 "yyyy/MM/dd" 格式（前端格式）
            if (dateStr.contains("/")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                return LocalDate.parse(dateStr, formatter);
            }
            // 尝试解析 "yyyy-MM-dd" 格式（数据库格式）
            else if (dateStr.contains("-")) {
                return LocalDate.parse(dateStr); // 使用默认的ISO格式解析器
            }
            // 如果都不匹配，抛出异常
            else {
                throw new IllegalArgumentException("不支持的日期格式，请使用 yyyy/MM/dd 或 yyyy-MM-dd 格式");
            }
        } catch (Exception e) {
            throw new Exception("日期格式错误，请使用 yyyy/MM/dd 或 yyyy-MM-dd 格式，输入值: " + dateStr + "，错误: " + e.getMessage());
        }
    }
    
    /**
     * 安全地解析日期，如果解析失败返回null
     * @param dateStr 日期字符串
     * @return LocalDate对象或null
     */
    public static LocalDate parseDateSafely(String dateStr) {
        try {
            return parseDate(dateStr);
        } catch (Exception e) {
            return null;
        }
    }
}