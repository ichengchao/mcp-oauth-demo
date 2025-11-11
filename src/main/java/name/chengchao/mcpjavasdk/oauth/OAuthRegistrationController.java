package name.chengchao.mcpjavasdk.oauth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * OAuth 2.0 动态客户端注册端点
 * RFC 7591: OAuth 2.0 Dynamic Client Registration Protocol
 */
@RestController
@RequestMapping("/oauth")
public class OAuthRegistrationController {

    private final OAuthService oauthService;

    public OAuthRegistrationController(OAuthService oauthService) {
        this.oauthService = oauthService;
    }

    /**
     * 动态客户端注册
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerClient(@RequestBody Map<String, Object> request) {
        try {
            // 提取请求参数
            List<String> redirectUris = extractList(request.get("redirect_uris"));
            List<String> grantTypes = extractList(request.get("grant_types"));
            List<String> responseTypes = extractList(request.get("response_types"));
            String clientName = (String) request.get("client_name");

            // 设置默认值
            if (grantTypes == null || grantTypes.isEmpty()) {
                grantTypes = List.of("authorization_code", "refresh_token");
            }
            if (responseTypes == null || responseTypes.isEmpty()) {
                responseTypes = List.of("code");
            }
            if (clientName == null || clientName.isEmpty()) {
                clientName = "Unnamed Client";
            }

            // 验证重定向URI
            if (redirectUris == null || redirectUris.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "invalid_redirect_uri",
                                "error_description", "redirect_uris is required"));
            }

            // 注册客户端
            OAuthClient client = oauthService.registerClient(redirectUris, grantTypes, responseTypes, clientName);

            // 构造响应
            Map<String, Object> response = new HashMap<>();
            response.put("client_id", client.getClientId());
            response.put("client_secret", client.getClientSecret());
            response.put("client_name", client.getClientName());
            response.put("redirect_uris", client.getRedirectUris());
            response.put("grant_types", client.getGrantTypes());
            response.put("response_types", client.getResponseTypes());
            response.put("client_id_issued_at", client.getCreatedAt().getEpochSecond());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "server_error",
                            "error_description", "Failed to register client: " + e.getMessage()));
        }
    }

    /**
     * 查询已注册的客户端列表（仅用于调试）
     */
    @GetMapping("/clients")
    public ResponseEntity<List<Map<String, Object>>> listClients() {
        List<Map<String, Object>> clientList = new ArrayList<>();

        for (OAuthClient client : oauthService.getAllClients()) {
            Map<String, Object> clientInfo = new HashMap<>();
            clientInfo.put("client_id", client.getClientId());
            clientInfo.put("client_name", client.getClientName());
            clientInfo.put("redirect_uris", client.getRedirectUris());
            clientInfo.put("grant_types", client.getGrantTypes());
            clientInfo.put("response_types", client.getResponseTypes());
            clientList.add(clientInfo);
        }

        return ResponseEntity.ok(clientList);
    }

    @SuppressWarnings("unchecked")
    private List<String> extractList(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof List) {
            return (List<String>) value;
        }
        if (value instanceof String) {
            return List.of((String) value);
        }
        return null;
    }
}
