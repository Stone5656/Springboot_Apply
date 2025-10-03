package com.example.config;

import com.example.security.JwtAuthenticationFilter;
import com.example.security.SecurityRuleApplier;
import com.example.security.SecurityRulesProperties;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.cors.*;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration // ← これがないとBean登録されない
@EnableConfigurationProperties(SecurityRulesProperties.class) // ← YAMLバインド
@RequiredArgsConstructor
public class SecurityConfig {
    private final SecurityRuleApplier ruleApplier;

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final SecurityRulesProperties securityRules; // ← application.yml の app.security.rules
    private final JwtAuthenticationFilter jwtAuthFilter = null;

    @PostConstruct
    void logLoadedSecurityRules() {
        var rules = securityRules.getRules();
        if (rules == null || rules.isEmpty()) {
            log.warn("app.security.rules is EMPTY. Fallback to built-in defaults.");
        } else {
            log.info("Loaded {} security rule(s) from YAML:", rules.size());
            for (int i = 0; i < rules.size(); i++) {
                var r = rules.get(i);
                log.info("[{}] pattern='{}', methods={}, access={}, authorities={}",
                        i, r.getPattern(), r.getMethods(), r.getAccess(), r.getAuthorities());
            }
        }
    }

    // パスワードエンコーダー
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // セキュリティの全体設定（YAML→適用、空なら従来の静的設定）
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           HandlerMappingIntrospector introspector,
                                           CorsConfigurationSource corsConfigurationSource) throws Exception {

        // Spring MVC と同じ URL マッチング（変数解決可）
        // refs: Spring Security doc「MvcRequestMatcher」
        var mvc = new MvcRequestMatcher.Builder(introspector);  // .pattern(HttpMethod.GET, "/path")
        // まずは共通のベース設定
        http
          .csrf(csrf -> csrf.disable())
          .formLogin(login -> login.disable())
          .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .cors(cors -> cors.configurationSource(corsConfigurationSource));

        // ← ここで YAML ルールを適用（あなたの SecurityRuleApplier のシグネチャに一致）
        ruleApplier.apply(http, securityRules, mvc);  // ★重要

        // 例外ハンドリング（お好みで）
        http.exceptionHandling(ex -> ex
            .authenticationEntryPoint((req, res, e) -> {
                if (res.isCommitted()) return;
                res.setStatus(401);
                res.setHeader("WWW-Authenticate", "Bearer");
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"unauthorized\"}");
            })
            .accessDeniedHandler((req, res, e) -> {
                if (res.isCommitted()) return;
                res.setStatus(403);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"forbidden\"}");
            })
        );

        if (jwtAuthFilter != null) {
            http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        }
        return http.build();
    }

    // === CORS設定 ===
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4000"));
        // OPTIONS を含める（プリフライト用）
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "Access-Control-Request-Headers"));
        config.setExposedHeaders(List.of("WWW-Authenticate"));
        config.setAllowCredentials(true);
        config.setMaxAge(java.time.Duration.ofHours(1));

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
