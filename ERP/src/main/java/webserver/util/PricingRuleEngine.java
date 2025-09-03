package webserver.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 定价规则计算引擎
 * 支持前序遍历表达式计算
 */
public class PricingRuleEngine {
    
    /**
     * 使用规则计算价格
     * @param rule 前序遍历表达式规则
     * @param x amountPre - 此前计算好的当前价格/单位
     * @param d amountDelta - 当前pricingElement的amount
     * @param p per - 当前amount对应的数量
     * @return 计算后的价格/单位
     */
    public static BigDecimal useRule(String rule, BigDecimal x, BigDecimal d, BigDecimal p) {
        try {
            // 替换变量
            String processedRule = rule
                    .replace("{x}", "{" + x.toString() + "}")
                    .replace("{d}", "{" + d.toString() + "}")
                    .replace("{p}", "{" + p.toString() + "}");

            return evaluatePrefixExpression(processedRule);
        } catch (Exception e) {
            System.err.println("计算定价规则时发生错误: " + e.getMessage());
            System.err.println("规则: " + rule + ", x=" + x + ", d=" + d + ", p=" + p);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 计算前序遍历表达式
     * @param expression 表达式字符串
     * @return 计算结果
     */
    private static BigDecimal evaluatePrefixExpression(String expression) {
        return parseExpression(expression, new int[]{0});
    }

    /**
     * 递归解析前序表达式
     * @param expression 表达式字符串
     * @param pos 当前解析位置（数组用于引用传递）
     * @return 计算结果
     */
    private static BigDecimal parseExpression(String expression, int[] pos) {
        if (pos[0] >= expression.length()) {
            return BigDecimal.ZERO;
        }

        char currentChar = expression.charAt(pos[0]);

        // 如果是操作符
        if (isOperator(String.valueOf(currentChar))) {
            pos[0]++; // 跳过操作符
            BigDecimal left = parseExpression(expression, pos);
            BigDecimal right = parseExpression(expression, pos);
            return applyOperator(String.valueOf(currentChar), left, right);
        }
        // 如果是花括号包围的数字
        else if (currentChar == '{') {
            pos[0]++; // 跳过 '{'
            StringBuilder numberStr = new StringBuilder();
            while (pos[0] < expression.length() && expression.charAt(pos[0]) != '}') {
                numberStr.append(expression.charAt(pos[0]));
                pos[0]++;
            }
            if (pos[0] < expression.length()) {
                pos[0]++; // 跳过 '}'
            }
            return new BigDecimal(numberStr.toString());
        }
        // 如果是直接的数字（不在花括号中）
        else if (Character.isDigit(currentChar) || currentChar == '.') {
            StringBuilder numberStr = new StringBuilder();
            while (pos[0] < expression.length() &&
                   (Character.isDigit(expression.charAt(pos[0])) || expression.charAt(pos[0]) == '.')) {
                numberStr.append(expression.charAt(pos[0]));
                pos[0]++;
            }
            return new BigDecimal(numberStr.toString());
        }

        // 跳过无法识别的字符
        pos[0]++;
        return BigDecimal.ZERO;
    }
    
    /**
     * 判断是否为操作符
     * @param token token
     * @return 是否为操作符
     */
    private static boolean isOperator(String token) {
        return "+".equals(token) || "-".equals(token) || "*".equals(token) || "/".equals(token);
    }
    
    /**
     * 应用操作符
     * @param operator 操作符
     * @param left 左操作数
     * @param right 右操作数
     * @return 计算结果
     */
    private static BigDecimal applyOperator(String operator, BigDecimal left, BigDecimal right) {
        switch (operator) {
            case "+":
                return left.add(right);
            case "-":
                return left.subtract(right);
            case "*":
                return left.multiply(right);
            case "/":
                if (right.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ArithmeticException("除数不能为零");
                }
                return left.divide(right, 10, RoundingMode.HALF_UP);
            default:
                throw new IllegalArgumentException("未知操作符: " + operator);
        }
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        // 测试 BASE: /{d}{p} = d/p
        BigDecimal result1 = useRule("/{d}{p}", BigDecimal.ZERO, new BigDecimal("100"), new BigDecimal("2"));
        System.out.println("BASE测试: 100/2 = " + result1); // 应该是 50

        // 测试 DCBV: -{x}/{d}{p} = x - d/p
        BigDecimal result2 = useRule("-{x}/{d}{p}", new BigDecimal("100"), new BigDecimal("20"), new BigDecimal("2"));
        System.out.println("DCBV测试: 100 - 20/2 = " + result2); // 应该是 90

        // 测试 DCBP: *{x}-{1}/{d}{100} = x * (1 - d/100)
        BigDecimal result3 = useRule("*{x}-{1}/{d}{100}", new BigDecimal("100"), new BigDecimal("10"), new BigDecimal("1"));
        System.out.println("DCBP测试: 100 * (1 - 10/100) = " + result3); // 应该是 90

        // 额外测试：简单加法
        BigDecimal result4 = useRule("+{x}{d}", new BigDecimal("50"), new BigDecimal("25"), new BigDecimal("1"));
        System.out.println("加法测试: 50 + 25 = " + result4); // 应该是 75
    }
}
