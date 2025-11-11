package name.chengchao.mcpjavasdk.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

/**
 * 简单的计算器工具类
 */

@Service
public class CalculatorTools {

    @Tool(description = "将两个数字相加")
    public int add(int a, int b) {
        return a + b;
    }

    @Tool(description = "计算两个数字的差")
    public int subtract(int a, int b) {
        return a - b;
    }

    @Tool(description = "将两个数字相乘")
    public int multiply(int a, int b) {
        return a * b;
    }

    @Tool(description = "计算两个数字的商")
    public double divide(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException("除数不能为0");
        }
        return (double)a / b;
    }
}
