package name.chengchao.mcpjavasdk.model;

/**
 * 工资信息实体类
 */
public class SalaryInfo {

    private String employeeId;
    private String employeeName;
    private double baseSalary;
    private double bonus;
    private double totalSalary;
    private String month;

    public SalaryInfo() {
    }

    public SalaryInfo(String employeeId, String employeeName, double baseSalary, double bonus, String month) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.baseSalary = baseSalary;
        this.bonus = bonus;
        this.totalSalary = baseSalary + bonus;
        this.month = month;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(double baseSalary) {
        this.baseSalary = baseSalary;
    }

    public double getBonus() {
        return bonus;
    }

    public void setBonus(double bonus) {
        this.bonus = bonus;
    }

    public double getTotalSalary() {
        return totalSalary;
    }

    public void setTotalSalary(double totalSalary) {
        this.totalSalary = totalSalary;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }
}
