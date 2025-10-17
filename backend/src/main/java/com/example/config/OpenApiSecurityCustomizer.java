// src/main/java/com/example/config/OpenApiSecurityCustomizer.java
package com.example.config;

import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import io.swagger.v3.oas.models.OpenAPI;

/**
 * Controllerやクラスに付いた @SecurityRequirement を維持しつつ、
 * 指定パスだけ Swagger 上で認証不要(= security要件なし)にする。
 */
@Configuration
@EnableConfigurationProperties(OpenApiSecurityProperties.class)
public class OpenApiSecurityCustomizer {

    @Bean
    OpenApiCustomizer relaxSecurityOnAnonymousPaths(OpenApiSecurityProperties props) {
        final AntPathMatcher matcher = new AntPathMatcher();
        final List<String> patterns = props.getAnonymousPaths();

        return (OpenAPI openApi) -> {
            if (openApi.getPaths() == null || patterns == null || patterns.isEmpty()) return;

            openApi.getPaths().forEach((path, item) -> {
                boolean isAnonymous = patterns.stream().anyMatch(p -> matcher.match(p, path));
                if (isAnonymous && item.readOperations() != null) {
                    item.readOperations().forEach(op -> op.setSecurity(List.of())); // ← securityを外す
                }
            });
        };
    }
}
