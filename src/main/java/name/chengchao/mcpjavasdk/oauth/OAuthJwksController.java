package name.chengchao.mcpjavasdk.oauth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * OAuth JWKS (JSON Web Key Set) 端点
 * Mock实现，返回一个假的公钥
 */
@RestController
@RequestMapping("/oauth")
public class OAuthJwksController {

    /**
     * JWKS端点
     * 返回用于验证JWT签名的公钥集合
     */
    @GetMapping("/keys")
    public Map<String, Object> jwks() {
        Map<String, Object> response = new HashMap<>();

        // Mock JWKS - 实际应用中应该返回真实的公钥
        List<Map<String, Object>> keys = new ArrayList<>();

        Map<String, Object> key = new HashMap<>();
        key.put("kty", "RSA");
        key.put("use", "sig");
        key.put("kid", "mock-key-1");
        key.put("alg", "RS256");
        key.put("n", "xGOr-H7A-PWgQwz6Csl1LhPTJTu6dINMfp0HCYVsXBMrRAkSYzQ9UpWmjZ8MfFMq0jPvIMMlnYLgYWmxwrvFZhh8F2bE7P6qxLn2Y9HZFnHKJ-wqXjJJqLRBKOlqHQPPNQ");
        key.put("e", "AQAB");

        keys.add(key);
        response.put("keys", keys);

        return response;
    }
}
