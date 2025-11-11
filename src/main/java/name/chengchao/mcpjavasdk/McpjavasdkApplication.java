package name.chengchao.mcpjavasdk;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import name.chengchao.mcpjavasdk.tools.CalculatorTools;
import name.chengchao.mcpjavasdk.tools.SalaryTool;

@SpringBootApplication
public class McpjavasdkApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpjavasdkApplication.class, args);
	}

	@Bean
    public ToolCallbackProvider calculatorToolsProvider(CalculatorTools calculatorTools) {
        return MethodToolCallbackProvider.builder()
            .toolObjects(calculatorTools)
            .build();
    }

	@Bean
	public ToolCallbackProvider salaryToolProvider(SalaryTool salaryTool) {
		return MethodToolCallbackProvider.builder()
			.toolObjects(salaryTool)
			.build();
	}

}
