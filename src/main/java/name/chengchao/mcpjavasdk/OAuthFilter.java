package name.chengchao.mcpjavasdk;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import name.chengchao.mcpjavasdk.oauth.OAuthService;

/**
 * OAuth Token 验证过滤器 - 从JWT中解析用户信息
 */
@Component
public class OAuthFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(OAuthFilter.class);

    private final OAuthService oauthService;

    public OAuthFilter(OAuthService oauthService) {
        this.oauthService = oauthService;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("OAuth filter initialized (JWT mode - no expiry check)");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestPath = httpRequest.getRequestURI();

        // 跳过OAuth流程相关的端点和well-known端点
        if (requestPath.startsWith("/oauth/") ||
            requestPath.startsWith("/.well-known/")) {
            chain.doFilter(request, response);
            return;
        }

        // 获取 Authorization header
        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header for path: {}", requestPath);
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        // 提取 token
        String token = authHeader.substring(7);

        // 从JWT中解析用户信息（不校验有效期）
        Optional<Map<String, String>> userInfoOpt = oauthService.parseJwtToken(token);

        if (userInfoOpt.isEmpty()) {
            logger.warn("Invalid JWT token for path: {}", requestPath);
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            return;
        }

        Map<String, String> userInfo = userInfoOpt.get();
        logger.debug("Token parsed successfully for client: {}, user: {}, scope: {}",
                userInfo.get("client_id"), userInfo.get("username"), userInfo.get("scope"));

        // 将token信息添加到request attributes，供后续使用
        httpRequest.setAttribute("oauth.client_id", userInfo.get("client_id"));
        httpRequest.setAttribute("oauth.scope", userInfo.get("scope"));
        httpRequest.setAttribute("oauth.username", userInfo.get("username"));
        httpRequest.setAttribute("oauth.employee_id", userInfo.get("employee_id"));

        // 继续执行
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
