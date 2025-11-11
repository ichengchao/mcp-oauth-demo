package name.chengchao.mcpjavasdk.oauth;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import name.chengchao.mcpjavasdk.model.User;
import name.chengchao.mcpjavasdk.service.SalaryService;

/**
 * OAuth 服务类，管理客户端、授权码和令牌
 */
@Service
public class OAuthService {

    private final Map<String, OAuthClient> clients = new ConcurrentHashMap<>();
    private final Map<String, AuthorizationCode> authorizationCodes = new ConcurrentHashMap<>();
    // 不再需要内存存储token
    // private final Map<String, AccessToken> accessTokens = new ConcurrentHashMap<>();
    private final Map<String, String> refreshTokenMap = new ConcurrentHashMap<>();

    private final SalaryService salaryService;

    // JWT 签名密钥 (32字节，用于HS256)
    private static final String JWT_SECRET = "mcpjavasdk_secret_key_for_jwt_signing_must_be_32_bytes";

    public OAuthService(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    /**
     * 注册新客户端
     */
    public OAuthClient registerClient(List<String> redirectUris, List<String> grantTypes,
                                     List<String> responseTypes, String clientName) {
        String clientId = "client_" + UUID.randomUUID().toString().replace("-", "");
        String clientSecret = "secret_" + UUID.randomUUID().toString().replace("-", "");

        OAuthClient client = new OAuthClient(clientId, clientSecret, redirectUris,
                grantTypes, responseTypes, clientName);
        clients.put(clientId, client);
        return client;
    }

    /**
     * 获取客户端
     */
    public Optional<OAuthClient> getClient(String clientId) {
        return Optional.ofNullable(clients.get(clientId));
    }

    /**
     * 验证客户端密钥
     */
    public boolean validateClientSecret(String clientId, String clientSecret) {
        return getClient(clientId)
                .map(client -> client.getClientSecret().equals(clientSecret))
                .orElse(false);
    }

    /**
     * 验证重定向URI
     */
    public boolean validateRedirectUri(String clientId, String redirectUri) {
        return getClient(clientId)
                .map(client -> client.getRedirectUris().contains(redirectUri))
                .orElse(false);
    }

    /**
     * 创建授权码（兼容旧方法）
     */
    public AuthorizationCode createAuthorizationCode(String clientId, String redirectUri, String scope) {
        return createAuthorizationCode(clientId, redirectUri, scope, null, null, null, null);
    }

    /**
     * 创建授权码（支持PKCE和用户信息）
     */
    public AuthorizationCode createAuthorizationCode(String clientId, String redirectUri, String scope,
                                                    String codeChallenge, String codeChallengeMethod,
                                                    String username, String employeeId) {
        String code = "code_" + UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plusSeconds(600); // 10分钟过期

        AuthorizationCode authCode = new AuthorizationCode(code, clientId, redirectUri, scope, expiresAt,
                codeChallenge, codeChallengeMethod);
        authCode.setUsername(username);
        authCode.setEmployeeId(employeeId);
        authorizationCodes.put(code, authCode);
        return authCode;
    }

    /**
     * 获取并验证授权码
     */
    public Optional<AuthorizationCode> getAuthorizationCode(String code) {
        AuthorizationCode authCode = authorizationCodes.get(code);
        if (authCode == null || authCode.isUsed() || authCode.isExpired()) {
            return Optional.empty();
        }
        return Optional.of(authCode);
    }

    /**
     * 标记授权码为已使用
     */
    public void markAuthorizationCodeAsUsed(String code) {
        AuthorizationCode authCode = authorizationCodes.get(code);
        if (authCode != null) {
            authCode.setUsed(true);
        }
    }

    /**
     * 创建访问令牌（包含用户信息）- JWT格式
     */
    public AccessToken createAccessToken(String clientId, String scope, String username, String employeeId) {
        String refreshToken = "refresh_" + UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plusSeconds(3600); // 1小时过期

        // 创建JWT token，包含用户信息
        String jwtToken = createJwtToken(clientId, scope, username, employeeId, expiresAt);

        AccessToken accessToken = new AccessToken(jwtToken, clientId, scope, expiresAt, refreshToken,
                username, employeeId);

        // 不再存储到内存中
        // accessTokens.put(jwtToken, accessToken);
        refreshTokenMap.put(refreshToken, jwtToken);
        return accessToken;
    }

    /**
     * 创建JWT token
     */
    private String createJwtToken(String clientId, String scope, String username, String employeeId, Instant expiresAt) {
        try {
            // 创建JWT claims
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(username)
                    .claim("client_id", clientId)
                    .claim("scope", scope)
                    .claim("username", username)
                    .claim("employee_id", employeeId)
                    .issueTime(new Date())
                    .expirationTime(Date.from(expiresAt))
                    .build();

            // 创建签名的JWT
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claimsSet);

            // 使用密钥签名
            JWSSigner signer = new MACSigner(JWT_SECRET.getBytes());
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to create JWT token", e);
        }
    }

    /**
     * 解析JWT token（不校验有效期）
     */
    public Optional<Map<String, String>> parseJwtToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            // 提取用户信息
            Map<String, String> userInfo = new java.util.HashMap<>();
            userInfo.put("username", claims.getStringClaim("username"));
            userInfo.put("employee_id", claims.getStringClaim("employee_id"));
            userInfo.put("client_id", claims.getStringClaim("client_id"));
            userInfo.put("scope", claims.getStringClaim("scope"));

            return Optional.of(userInfo);
        } catch (Exception e) {
            // 解析失败
            return Optional.empty();
        }
    }

    /**
     * 通过刷新令牌获取新的访问令牌
     */
    public Optional<AccessToken> refreshAccessToken(String refreshToken) {
        String oldToken = refreshTokenMap.get(refreshToken);
        if (oldToken == null) {
            return Optional.empty();
        }

        // 从JWT token中解析用户信息
        Optional<Map<String, String>> userInfoOpt = parseJwtToken(oldToken);
        if (userInfoOpt.isEmpty()) {
            return Optional.empty();
        }

        Map<String, String> userInfo = userInfoOpt.get();

        // 创建新的访问令牌
        return Optional.of(createAccessToken(
                userInfo.get("client_id"),
                userInfo.get("scope"),
                userInfo.get("username"),
                userInfo.get("employee_id")));
    }

    /**
     * 获取所有已注册的客户端
     */
    public List<OAuthClient> getAllClients() {
        return new ArrayList<>(clients.values());
    }

    /**
     * 验证 PKCE code_verifier (Mock实现 - 总是通过)
     */
    public boolean validatePKCE(AuthorizationCode authCode, String codeVerifier) {
        // Mock实现：只要有code_challenge就需要code_verifier，否则总是通过
        String codeChallenge = authCode.getCodeChallenge();

        if (codeChallenge == null || codeChallenge.isEmpty()) {
            // 没有使用PKCE，直接通过
            return true;
        }

        // 有PKCE，检查是否提供了code_verifier（Mock：不做实际验证）
        return codeVerifier != null && !codeVerifier.isEmpty();
    }

    /**
     * 验证用户名密码
     */
    public Optional<User> authenticateUser(String username, String password) {
        return salaryService.authenticate(username, password);
    }
}
