package name.chengchao.mcpjavasdk;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OAuth Well-Known 端点控制器
 */
@RestController
public class OAuthWellKnownController {

    @Value("${oauth.issuer}")
    private String issuer;

    @Value("${server.port}")
    private int serverPort;

    /**
     * OAuth 授权服务器元数据
     * RFC 8414: OAuth 2.0 Authorization Server Metadata
     */
    @GetMapping("/.well-known/oauth-authorization-server")
    public Map<String, Object> authorizationServerMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("issuer", issuer);
        metadata.put("authorization_endpoint", issuer + "/oauth/authorize");
        metadata.put("token_endpoint", issuer + "/oauth/token");
        metadata.put("jwks_uri", issuer + "/oauth/keys");
        metadata.put("registration_endpoint", issuer + "/oauth/register");

        metadata.put("response_types_supported", List.of("code"));
        metadata.put("grant_types_supported", List.of("authorization_code", "refresh_token"));
        metadata.put("token_endpoint_auth_methods_supported", List.of("client_secret_basic", "client_secret_post"));
        metadata.put("subject_types_supported", List.of("public"));
        metadata.put("scopes_supported", List.of("openid", "profile", "email"));
        metadata.put("id_token_signing_alg_values_supported", List.of("RS256"));

        // PKCE support
        metadata.put("code_challenge_methods_supported", List.of("S256", "plain"));

        return metadata;
    }

    /**
     * OAuth 受保护资源元数据
     * RFC 8705: OAuth 2.0 Resource Indicators
     */
    @GetMapping("/.well-known/oauth-protected-resource")
    public Map<String, Object> protectedResourceMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        // 资源服务器信息
        metadata.put("resource", "http://localhost:" + serverPort);
        metadata.put("authorization_servers", List.of(issuer));

        // MCP API 端点信息
        Map<String, Object> mcpEndpoint = new HashMap<>();
        mcpEndpoint.put("path", "/api/mcp");
        mcpEndpoint.put("scopes_required", List.of("openid"));
        mcpEndpoint.put("authentication_methods", List.of("bearer"));

        metadata.put("bearer_methods_supported", List.of("header"));
        metadata.put("resource_documentation", "http://localhost:" + serverPort + "/api/mcp");
        metadata.put("resource_signing_alg_values_supported", List.of("RS256"));

        // 额外的 MCP 特定信息
        Map<String, Object> mcpInfo = new HashMap<>();
        mcpInfo.put("protocol", "STREAMABLE");
        mcpInfo.put("name", "calculator-server");
        mcpInfo.put("version", "1.0.0");
        mcpInfo.put("endpoint", "/api/mcp");

        metadata.put("mcp_server_info", mcpInfo);

        return metadata;
    }
}
