package name.chengchao.mcpjavasdk.oauth;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import name.chengchao.mcpjavasdk.model.User;

/**
 * OAuth 2.0 授权端点
 */
@Controller
@RequestMapping("/oauth")
public class OAuthAuthorizationController {

    private final OAuthService oauthService;

    public OAuthAuthorizationController(OAuthService oauthService) {
        this.oauthService = oauthService;
    }

    /**
     * 授权端点 - 显示授权页面
     */
    @GetMapping("/authorize")
    public String authorize(
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("response_type") String responseType,
            @RequestParam(value = "scope", required = false, defaultValue = "openid") String scope,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "code_challenge", required = false) String codeChallenge,
            @RequestParam(value = "code_challenge_method", required = false, defaultValue = "plain") String codeChallengeMethod,
            Model model,
            HttpServletResponse response) throws IOException {

        // 验证client_id
        Optional<OAuthClient> clientOpt = oauthService.getClient(clientId);
        if (clientOpt.isEmpty()) {
            return redirectError(response, redirectUri, "invalid_client", "Invalid client_id", state);
        }

        OAuthClient client = clientOpt.get();

        // 验证response_type
        if (!"code".equals(responseType)) {
            return redirectError(response, redirectUri, "unsupported_response_type",
                    "Only 'code' response_type is supported", state);
        }

        // 验证redirect_uri
        if (!oauthService.validateRedirectUri(clientId, redirectUri)) {
            // 如果redirect_uri无效，不能重定向到该URI
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid redirect_uri for client: " + clientId);
            return null;
        }

        // 验证 PKCE 参数
        if (codeChallenge != null && !codeChallenge.isEmpty()) {
            if (!"S256".equals(codeChallengeMethod) && !"plain".equals(codeChallengeMethod)) {
                return redirectError(response, redirectUri, "invalid_request",
                        "Unsupported code_challenge_method: " + codeChallengeMethod, state);
            }
        }

        // 重定向到登录页面，保留所有OAuth参数
        StringBuilder loginUrl = new StringBuilder("/login.html");
        loginUrl.append("?client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8));
        loginUrl.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
        loginUrl.append("&response_type=").append(URLEncoder.encode(responseType, StandardCharsets.UTF_8));
        loginUrl.append("&scope=").append(URLEncoder.encode(scope, StandardCharsets.UTF_8));
        if (state != null) {
            loginUrl.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
        }
        if (codeChallenge != null) {
            loginUrl.append("&code_challenge=").append(URLEncoder.encode(codeChallenge, StandardCharsets.UTF_8));
            loginUrl.append("&code_challenge_method=").append(URLEncoder.encode(codeChallengeMethod, StandardCharsets.UTF_8));
        }

        response.sendRedirect(loginUrl.toString());
        return null;
    }

    /**
     * 处理用户登录
     */
    @PostMapping("/login")
    public String login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam(value = "response_type", defaultValue = "code") String responseType,
            @RequestParam(value = "scope", defaultValue = "openid") String scope,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "code_challenge", required = false) String codeChallenge,
            @RequestParam(value = "code_challenge_method", required = false, defaultValue = "plain") String codeChallengeMethod,
            HttpServletResponse response) throws IOException {

        // 验证用户名密码
        Optional<User> userOpt = oauthService.authenticateUser(username, password);

        if (userOpt.isEmpty()) {
            // 登录失败，重定向回登录页面并带上错误信息
            StringBuilder loginUrl = new StringBuilder("/login.html");
            loginUrl.append("?error=").append(URLEncoder.encode("Invalid username or password", StandardCharsets.UTF_8));
            loginUrl.append("&client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8));
            loginUrl.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
            loginUrl.append("&response_type=").append(URLEncoder.encode(responseType, StandardCharsets.UTF_8));
            loginUrl.append("&scope=").append(URLEncoder.encode(scope, StandardCharsets.UTF_8));
            if (state != null) {
                loginUrl.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
            }
            if (codeChallenge != null) {
                loginUrl.append("&code_challenge=").append(URLEncoder.encode(codeChallenge, StandardCharsets.UTF_8));
                loginUrl.append("&code_challenge_method=").append(URLEncoder.encode(codeChallengeMethod, StandardCharsets.UTF_8));
            }
            response.sendRedirect(loginUrl.toString());
            return null;
        }

        User user = userOpt.get();

        // 创建授权码（包含用户信息）
        AuthorizationCode authCode = oauthService.createAuthorizationCode(clientId, redirectUri, scope,
                codeChallenge, codeChallengeMethod, user.getUsername(), user.getEmployeeId());

        // 构造重定向URL
        StringBuilder redirectUrl = new StringBuilder(redirectUri);
        redirectUrl.append(redirectUri.contains("?") ? "&" : "?");
        redirectUrl.append("code=").append(authCode.getCode());

        if (state != null && !state.isEmpty()) {
            redirectUrl.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
        }

        // 重定向回客户端
        response.sendRedirect(redirectUrl.toString());
        return null;
    }


    /**
     * 重定向错误响应
     */
    private String redirectError(HttpServletResponse response, String redirectUri,
                                 String error, String errorDescription, String state) throws IOException {
        StringBuilder redirectUrl = new StringBuilder(redirectUri);
        redirectUrl.append(redirectUri.contains("?") ? "&" : "?");
        redirectUrl.append("error=").append(URLEncoder.encode(error, StandardCharsets.UTF_8));
        redirectUrl.append("&error_description=")
                .append(URLEncoder.encode(errorDescription, StandardCharsets.UTF_8));

        if (state != null && !state.isEmpty()) {
            redirectUrl.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
        }

        response.sendRedirect(redirectUrl.toString());
        return null;
    }
}
