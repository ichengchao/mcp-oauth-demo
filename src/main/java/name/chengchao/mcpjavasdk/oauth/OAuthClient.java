package name.chengchao.mcpjavasdk.oauth;

import java.time.Instant;
import java.util.List;

/**
 * OAuth 客户端实体类
 */
public class OAuthClient {

    private String clientId;
    private String clientSecret;
    private List<String> redirectUris;
    private List<String> grantTypes;
    private List<String> responseTypes;
    private String clientName;
    private Instant createdAt;

    public OAuthClient() {
        this.createdAt = Instant.now();
    }

    public OAuthClient(String clientId, String clientSecret, List<String> redirectUris,
                       List<String> grantTypes, List<String> responseTypes, String clientName) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUris = redirectUris;
        this.grantTypes = grantTypes;
        this.responseTypes = responseTypes;
        this.clientName = clientName;
        this.createdAt = Instant.now();
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public List<String> getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(List<String> redirectUris) {
        this.redirectUris = redirectUris;
    }

    public List<String> getGrantTypes() {
        return grantTypes;
    }

    public void setGrantTypes(List<String> grantTypes) {
        this.grantTypes = grantTypes;
    }

    public List<String> getResponseTypes() {
        return responseTypes;
    }

    public void setResponseTypes(List<String> responseTypes) {
        this.responseTypes = responseTypes;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
