package name.chengchao.mcpjavasdk.oauth;

import java.time.Instant;

/**
 * OAuth 访问令牌实体类
 */
public class AccessToken {

    private String token;
    private String clientId;
    private String scope;
    private Instant expiresAt;
    private String refreshToken;

    // 用户信息
    private String username;
    private String employeeId;

    public AccessToken() {
    }

    public AccessToken(String token, String clientId, String scope, Instant expiresAt, String refreshToken) {
        this.token = token;
        this.clientId = clientId;
        this.scope = scope;
        this.expiresAt = expiresAt;
        this.refreshToken = refreshToken;
    }

    public AccessToken(String token, String clientId, String scope, Instant expiresAt, String refreshToken,
                      String username, String employeeId) {
        this.token = token;
        this.clientId = clientId;
        this.scope = scope;
        this.expiresAt = expiresAt;
        this.refreshToken = refreshToken;
        this.username = username;
        this.employeeId = employeeId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
}
