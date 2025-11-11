package name.chengchao.mcpjavasdk.tools;

import jakarta.servlet.http.HttpServletRequest;
import name.chengchao.mcpjavasdk.model.SalaryInfo;
import name.chengchao.mcpjavasdk.service.SalaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * 工资查询工具
 */
@Service
public class SalaryTool {

    private static final Logger logger = LoggerFactory.getLogger(SalaryTool.class);

    private final SalaryService salaryService;

    public SalaryTool(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    /**
     * 查询我的工资信息（当前月份）
     */
    @Tool(description = "Query my current month salary information. Returns salary details including base salary, bonus, and total salary.")
    public String getMySalary() {
        logger.info("========== SalaryTool.getMySalary() called ==========");
        logger.info("Input: No parameters");

        // 从请求中获取当前用户的员工ID
        String employeeId = getCurrentEmployeeId();
        logger.info("Employee ID from token: {}", employeeId);

        if (employeeId == null) {
            String errorMsg = "Error: User not authenticated or employee ID not found";
            logger.warn("Output: {}", errorMsg);
            logger.info("========== SalaryTool.getMySalary() completed ==========");
            return errorMsg;
        }

        // 查询工资信息
        Optional<SalaryInfo> salaryOpt = salaryService.getSalaryByEmployeeId(employeeId);

        if (salaryOpt.isEmpty()) {
            String errorMsg = "Error: Salary information not found for employee ID: " + employeeId;
            logger.warn("Output: {}", errorMsg);
            logger.info("========== SalaryTool.getMySalary() completed ==========");
            return errorMsg;
        }

        SalaryInfo salary = salaryOpt.get();

        // 返回格式化的工资信息
        String result = formatSalaryInfo(salary);
        logger.info("Output:\n{}", result);
        logger.info("========== SalaryTool.getMySalary() completed ==========");
        return result;
    }

    /**
     * 查询指定月份的工资信息
     */
    @Tool(description = "Query my salary information for a specific month. Month format should be YYYY-MM (e.g., 2025-01). Returns salary details for the specified month.")
    public String getMySalaryByMonth(String month) {
        logger.info("========== SalaryTool.getMySalaryByMonth() called ==========");
        logger.info("Input: month = {}", month);

        // 从请求中获取当前用户的员工ID
        String employeeId = getCurrentEmployeeId();
        logger.info("Employee ID from token: {}", employeeId);

        if (employeeId == null) {
            String errorMsg = "Error: User not authenticated or employee ID not found";
            logger.warn("Output: {}", errorMsg);
            logger.info("========== SalaryTool.getMySalaryByMonth() completed ==========");
            return errorMsg;
        }

        // 验证月份格式
        if (month == null || !month.matches("\\d{4}-\\d{2}")) {
            String errorMsg = "Error: Invalid month format. Please use YYYY-MM format (e.g., 2025-01)";
            logger.warn("Output: {}", errorMsg);
            logger.info("========== SalaryTool.getMySalaryByMonth() completed ==========");
            return errorMsg;
        }

        // 查询指定月份的工资信息
        Optional<SalaryInfo> salaryOpt = salaryService.getSalaryByEmployeeIdAndMonth(employeeId, month);

        if (salaryOpt.isEmpty()) {
            String errorMsg = "Error: Salary information not found for employee ID: " + employeeId + " in month: " + month;
            logger.warn("Output: {}", errorMsg);
            logger.info("========== SalaryTool.getMySalaryByMonth() completed ==========");
            return errorMsg;
        }

        SalaryInfo salary = salaryOpt.get();

        // 返回格式化的工资信息
        String result = formatSalaryInfo(salary);
        logger.info("Output:\n{}", result);
        logger.info("========== SalaryTool.getMySalaryByMonth() completed ==========");
        return result;
    }

    /**
     * 格式化工资信息
     */
    private String formatSalaryInfo(SalaryInfo salary) {
        return String.format("""
            Salary Information:
            - Employee: %s (%s)
            - Month: %s
            - Base Salary: ¥%.2f
            - Bonus: ¥%.2f
            - Total Salary: ¥%.2f
            """,
            salary.getEmployeeName(),
            salary.getEmployeeId(),
            salary.getMonth(),
            salary.getBaseSalary(),
            salary.getBonus(),
            salary.getTotalSalary()
        );
    }

    /**
     * 从当前请求中获取员工ID
     */
    private String getCurrentEmployeeId() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return (String) request.getAttribute("oauth.employee_id");
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return null;
    }
}
