package name.chengchao.mcpjavasdk.oauth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * OAuth 2.0 令牌端点
 */
@RestController
@RequestMapping("/oauth")
public class OAuthTokenController {

    private final OAuthService oauthService;

    public OAuthTokenController(OAuthService oauthService) {
        this.oauthService = oauthService;
    }

    /**
     * 令牌端点
     */
    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> token(
            @RequestParam("grant_type") String grantType,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "client_secret", required = false) String clientSecret,
            @RequestParam(value = "refresh_token", required = false) String refreshToken,
            @RequestParam(value = "code_verifier", required = false) String codeVerifier,
            HttpServletRequest request) {

        // 尝试从Authorization头获取客户端凭证
        String[] credentials = extractClientCredentials(request, clientId, clientSecret);
        clientId = credentials[0];
        clientSecret = credentials[1];

        // 验证客户端
        if (clientId == null || clientSecret == null) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "invalid_client",
                    "Client authentication failed");
        }

        if (!oauthService.validateClientSecret(clientId, clientSecret)) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "invalid_client",
                    "Invalid client credentials");
        }

        // 根据grant_type处理不同的流程
        return switch (grantType) {
            case "authorization_code" -> handleAuthorizationCodeGrant(code, redirectUri, clientId, codeVerifier);
            case "refresh_token" -> handleRefreshTokenGrant(refreshToken, clientId);
            default -> errorResponse(HttpStatus.BAD_REQUEST, "unsupported_grant_type",
                    "Grant type '" + grantType + "' is not supported");
        };
    }

    /**
     * 处理授权码授权
     */
    private ResponseEntity<Map<String, Object>> handleAuthorizationCodeGrant(
            String code, String redirectUri, String clientId, String codeVerifier) {

        if (code == null || redirectUri == null) {
            return errorResponse(HttpStatus.BAD_REQUEST, "invalid_request",
                    "Missing required parameters: code or redirect_uri");
        }

        // 验证授权码
        Optional<AuthorizationCode> authCodeOpt = oauthService.getAuthorizationCode(code);
        if (authCodeOpt.isEmpty()) {
            return errorResponse(HttpStatus.BAD_REQUEST, "invalid_grant",
                    "Invalid or expired authorization code");
        }

        AuthorizationCode authCode = authCodeOpt.get();

        // 验证客户端ID
        if (!authCode.getClientId().equals(clientId)) {
            return errorResponse(HttpStatus.BAD_REQUEST, "invalid_grant",
                    "Authorization code was issued to a different client");
        }

        // 验证重定向URI
        if (!authCode.getRedirectUri().equals(redirectUri)) {
            return errorResponse(HttpStatus.BAD_REQUEST, "invalid_grant",
                    "Redirect URI mismatch");
        }

        // 验证 PKCE code_verifier
        if (!oauthService.validatePKCE(authCode, codeVerifier)) {
            return errorResponse(HttpStatus.BAD_REQUEST, "invalid_grant",
                    "Invalid code_verifier");
        }

        // 标记授权码为已使用
        oauthService.markAuthorizationCodeAsUsed(code);

        // 创建访问令牌（包含用户信息）
        AccessToken accessToken = oauthService.createAccessToken(clientId, authCode.getScope(),
                authCode.getUsername(), authCode.getEmployeeId());

        // 返回令牌响应
        return successResponse(accessToken);
    }

    /**
     * 处理刷新令牌授权
     */
    private ResponseEntity<Map<String, Object>> handleRefreshTokenGrant(
            String refreshToken, String clientId) {

        if (refreshToken == null) {
            return errorResponse(HttpStatus.BAD_REQUEST, "invalid_request",
                    "Missing required parameter: refresh_token");
        }

        // 使用刷新令牌获取新的访问令牌
        Optional<AccessToken> oldAccessTokenOpt = oauthService.refreshAccessToken(refreshToken);
        if (oldAccessTokenOpt.isEmpty()) {
            return errorResponse(HttpStatus.BAD_REQUEST, "invalid_grant",
                    "Invalid refresh token");
        }

        AccessToken oldAccessToken = oldAccessTokenOpt.get();

        // 验证客户端ID
        if (!oldAccessToken.getClientId().equals(clientId)) {
            return errorResponse(HttpStatus.BAD_REQUEST, "invalid_grant",
                    "Refresh token was issued to a different client");
        }

        // 创建新的访问令牌（保留用户信息）
        AccessToken newAccessToken = oauthService.createAccessToken(clientId, oldAccessToken.getScope(),
                oldAccessToken.getUsername(), oldAccessToken.getEmployeeId());

        // 返回令牌响应
        return successResponse(newAccessToken);
    }

    /**
     * 从请求中提取客户端凭证
     */
    private String[] extractClientCredentials(HttpServletRequest request,
                                             String clientId, String clientSecret) {
        // 首先尝试从Basic Auth头获取
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String base64Credentials = authHeader.substring(6);
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            String[] parts = credentials.split(":", 2);
            if (parts.length == 2) {
                return new String[]{parts[0], parts[1]};
            }
        }

        // 否则使用请求参数
        return new String[]{clientId, clientSecret};
    }

    /**
     * 构造成功响应
     */
    private ResponseEntity<Map<String, Object>> successResponse(AccessToken accessToken) {
        Map<String, Object> response = new HashMap<>();
        response.put("access_token", accessToken.getToken());
        response.put("token_type", "Bearer");
        response.put("expires_in", accessToken.getExpiresAt().getEpochSecond() - System.currentTimeMillis() / 1000);
        response.put("refresh_token", accessToken.getRefreshToken());
        response.put("scope", accessToken.getScope());

        return ResponseEntity.ok(response);
    }

    /**
     * 构造错误响应
     */
    private ResponseEntity<Map<String, Object>> errorResponse(
            HttpStatus status, String error, String errorDescription) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", error);
        response.put("error_description", errorDescription);

        return ResponseEntity.status(status).body(response);
    }
}
