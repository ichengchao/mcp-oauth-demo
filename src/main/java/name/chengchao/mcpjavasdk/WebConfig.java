package name.chengchao.mcpjavasdk;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<OAuthFilter> oauthFilterRegistration(OAuthFilter oauthFilter) {
        FilterRegistrationBean<OAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(oauthFilter);

        // 只拦截 /api/mcp 路径
        registration.addUrlPatterns("/api/mcp", "/api/mcp/*");

        registration.setName("oauthFilter");
        registration.setOrder(1);

        return registration;
    }

    /**
     * 配置 CORS
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type")
                .allowCredentials(true)
                .maxAge(3600);
    }

    // 注意: /.well-known/* 端点不需要认证，所以不添加到 OAuth filter
}
