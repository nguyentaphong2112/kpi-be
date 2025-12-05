package vn.hbtplus.configs.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import vn.hbtplus.configs.AppConfigResource;
import vn.hbtplus.filters.AuthTokenFilter;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class DynamicSecurityConfig {

    private final AppConfigResource configResource;
    private final AuthTokenFilter authTokenFilter;

    /**
     * Đây là bean filter chính — chỉ duy nhất một SecurityFilterChain được tạo.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(getIgnoredMatchers()).permitAll()
                        .anyRequest().authenticated()
                );

        if (configResource.getSsoEnabled()) {
            // ✅ Dành cho môi trường có SSO
            log.info("✅ Using OAuth2 Resource Server (JWT from SSO)");
            http.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
        } else {
            // ✅ Dành cho môi trường nội bộ (không dùng SSO)
            log.info("✅ Using internal JWT filter (AuthTokenFilter)");
            http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }

    /**
     * Cấu hình URL bỏ qua xác thực.
     */
    private RequestMatcher[] getIgnoredMatchers() {
        return Arrays.stream(configResource.getUrlIgnoreToken().split(","))
                .map(String::trim)
                .map(AntPathRequestMatcher::new)
                .toArray(RequestMatcher[]::new);
    }

    /**
     * Cho phép CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}

