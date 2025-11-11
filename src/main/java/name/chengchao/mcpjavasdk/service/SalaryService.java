package name.chengchao.mcpjavasdk.service;

import name.chengchao.mcpjavasdk.model.SalaryInfo;
import name.chengchao.mcpjavasdk.model.User;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.*;

/**
 * 工资服务类 - Mock实现
 */
@Service
public class SalaryService {

    private final Map<String, User> users = new HashMap<>();
    // Map<EmployeeId, Map<Month, SalaryInfo>>
    private final Map<String, Map<String, SalaryInfo>> salaries = new HashMap<>();

    public SalaryService() {
        initMockData();
    }

    private void initMockData() {
        // Mock 3个用户 - 密码都是123
        users.put("alice", new User("alice", "123", "Alice Wang", "EMP001"));
        users.put("bob", new User("bob", "123", "Bob Li", "EMP002"));
        users.put("charles", new User("charles", "123", "Charles Zhang", "EMP003"));

        // Mock 12个月的工资数据
        initSalaryData("EMP001", "Alice Wang", 15000.00, 3000.00);
        initSalaryData("EMP002", "Bob Li", 18000.00, 4500.00);
        initSalaryData("EMP003", "Charles Zhang", 20000.00, 5000.00);
    }

    private void initSalaryData(String employeeId, String employeeName, double baseSalary, double baseBonus) {
        Map<String, SalaryInfo> monthlySalaries = new HashMap<>();

        // 为每个员工生成12个月的工资数据（2025年1月到12月）
        for (int month = 1; month <= 12; month++) {
            String monthStr = String.format("2025-%02d", month);

            // 每个月的奖金有轻微浮动（±20%）
            double bonusVariation = 1.0 + (Math.random() * 0.4 - 0.2);
            double monthlyBonus = baseBonus * bonusVariation;

            SalaryInfo salaryInfo = new SalaryInfo(
                employeeId,
                employeeName,
                baseSalary,
                Math.round(monthlyBonus * 100.0) / 100.0,
                monthStr
            );

            monthlySalaries.put(monthStr, salaryInfo);
        }

        salaries.put(employeeId, monthlySalaries);
    }

    /**
     * 验证用户名和密码
     */
    public Optional<User> authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    /**
     * 根据用户名获取用户
     */
    public Optional<User> getUserByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    /**
     * 根据员工ID获取当前月份工资信息
     */
    public Optional<SalaryInfo> getSalaryByEmployeeId(String employeeId) {
        // 默认返回当前月份
        String currentMonth = YearMonth.now().toString();
        return getSalaryByEmployeeIdAndMonth(employeeId, currentMonth);
    }

    /**
     * 根据员工ID和月份获取工资信息
     */
    public Optional<SalaryInfo> getSalaryByEmployeeIdAndMonth(String employeeId, String month) {
        Map<String, SalaryInfo> monthlySalaries = salaries.get(employeeId);
        if (monthlySalaries != null) {
            return Optional.ofNullable(monthlySalaries.get(month));
        }
        return Optional.empty();
    }

    /**
     * 获取员工所有月份的工资信息
     */
    public List<SalaryInfo> getAllSalariesByEmployeeId(String employeeId) {
        Map<String, SalaryInfo> monthlySalaries = salaries.get(employeeId);
        if (monthlySalaries != null) {
            return new ArrayList<>(monthlySalaries.values());
        }
        return Collections.emptyList();
    }

    /**
     * 获取所有用户（用于测试）
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}
