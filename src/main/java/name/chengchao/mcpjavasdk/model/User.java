package name.chengchao.mcpjavasdk.model;

/**
 * 用户实体类
 */
public class User {

    private String username;
    private String password;
    private String name;
    private String employeeId;

    public User() {
    }

    public User(String username, String password, String name, String employeeId) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.employeeId = employeeId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
}
