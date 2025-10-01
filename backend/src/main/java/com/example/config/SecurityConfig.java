package com.example.config;

import com.example.security.JwtAuthenticationFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration // ← これがないとBean登録されない
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    // パスワードエンコーダー定義
    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    // セキュリティの全体設定
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           HandlerMappingIntrospector introspector) throws Exception {
        var mvc = new MvcRequestMatcher.Builder(introspector);

        http
          .csrf(csrf -> csrf.disable())
          .formLogin(login -> login.disable())
          .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .cors(cors -> cors.configurationSource(corsConfigurationSource()))
          .authorizeHttpRequests(auth -> auth
              // 1) エラー/静的/プリフライト
              .requestMatchers("/error", "/error/**", "/favicon.ico", "/css/**", "/js/**", "/images/**", "/assets/**").permitAll()
              .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

              // 2) 公開したい一覧・閲覧系 (GET)
              .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/videos")).permitAll()               // 検索
              .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/videos/popular")).permitAll()
              .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/videos/recent")).permitAll()
              .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/videos/user/{userId}")).permitAll() // ユーザー別公開動画
              .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/videos/{id}")).permitAll()          // 個別動画参照

              // 3) 認証が必要な閲覧系 (GET)
              .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/videos/my")).authenticated()

              // 4) 書き込み系（POST/PUT/PATCH/DELETE）は認証必須
              .requestMatchers("/api/videos/**").authenticated()

              // 5) 既存の公開API
              .requestMatchers("/api/users/register", "/api/users/login", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/**").permitAll()

              // 6) 管理系
              .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

              // 7) その他は認証必須
              .anyRequest().authenticated()
          )
          // 401/403の本文を返したいとき（SwaggerのUndocumentedを避ける）
          .exceptionHandling(ex -> ex
              .authenticationEntryPoint((req, res, e) -> {
                  if (res.isCommitted()) return;
                  res.setStatus(401);
                  res.setContentType("application/json");
                  res.setHeader("WWW-Authenticate", "Bearer"); // RFC6750
                  res.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"Missing or invalid Bearer token\"}");
              })
              .accessDeniedHandler((req, res, e) -> {
                  if (res.isCommitted()) return;
                  res.setStatus(403);
                  res.setContentType("application/json");
                  res.getWriter().write("{\"error\":\"forbidden\"}");
              })
          )
          .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // === CORS設定！！！ ===
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        var config = new org.springframework.web.cors.CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
            "Authorization", "Content-Type", "Accept", "Origin", "Access-Control-Request-Headers"));
        // BearerTokenEntryPointが付与するWWW-Authenticateをクライアントで読めるように
        config.setExposedHeaders(List.of("WWW-Authenticate"));
        config.setAllowCredentials(true);
        config.setMaxAge(java.time.Duration.ofHours(1));

        var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception
    {
        return config.getAuthenticationManager();
    }
}
