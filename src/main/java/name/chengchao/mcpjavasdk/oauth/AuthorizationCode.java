package name.chengchao.mcpjavasdk.oauth;

import java.time.Instant;

/**
 * OAuth 授权码实体类
 */
public class AuthorizationCode {

    private String code;
    private String clientId;
    private String redirectUri;
    private String scope;
    private Instant expiresAt;
    private boolean used;

    // PKCE fields
    private String codeChallenge;
    private String codeChallengeMethod;

    // 用户信息
    private String username;
    private String employeeId;

    public AuthorizationCode() {
    }

    public AuthorizationCode(String code, String clientId, String redirectUri, String scope, Instant expiresAt) {
        this.code = code;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.scope = scope;
        this.expiresAt = expiresAt;
        this.used = false;
    }

    public AuthorizationCode(String code, String clientId, String redirectUri, String scope, Instant expiresAt,
                           String codeChallenge, String codeChallengeMethod) {
        this.code = code;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.scope = scope;
        this.expiresAt = expiresAt;
        this.used = false;
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
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

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public void setCodeChallenge(String codeChallenge) {
        this.codeChallenge = codeChallenge;
    }

    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    public void setCodeChallengeMethod(String codeChallengeMethod) {
        this.codeChallengeMethod = codeChallengeMethod;
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
