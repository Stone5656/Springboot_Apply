// src/main/java/com/example/security/SecurityRuleApplier.java
package com.example.security;

import static com.example.security.SecurityRulesProperties.Rule.Access;
import static org.springframework.util.StringUtils.hasText;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizedUrl;

@Component
public class SecurityRuleApplier {

  public void apply(HttpSecurity http,
                    SecurityRulesProperties props,
                    MvcRequestMatcher.Builder mvc) throws Exception {

    http.authorizeHttpRequests(reg -> {
      for (var rule : props.getRules()) {
        final var pattern = Objects.requireNonNull(rule.getPattern(), "pattern is required");
        final var methods = rule.getMethods();

        if (CollectionUtils.isEmpty(methods)) {
          AuthorizedUrl url = reg.requestMatchers(mvc.pattern(pattern));
          applyAccess(url, rule);
        } else {
          for (String m : methods) {
            final HttpMethod hm;
            try {
              hm = HttpMethod.valueOf(m.toUpperCase());
            } catch (IllegalArgumentException ex) {
              throw new IllegalArgumentException("Unsupported HTTP method in YAML: " + m, ex);
            }
            AuthorizedUrl url = reg.requestMatchers(mvc.pattern(hm, pattern));
            applyAccess(url, rule);
          }
        }
      }
      reg.anyRequest().denyAll();
    });
  }

  public void apply(HttpSecurity http,
                    SecurityRulesProperties props) throws Exception {

    http.authorizeHttpRequests(reg -> {
      for (var rule : props.getRules()) {
        final var pattern = Objects.requireNonNull(rule.getPattern(), "pattern is required");
        final var methods = rule.getMethods();

        if (CollectionUtils.isEmpty(methods)) {
          AuthorizedUrl url = reg.requestMatchers(new AntPathRequestMatcher(pattern));
          applyAccess(url, rule);
        } else {
          for (String m : methods) {
            final HttpMethod hm;
            try {
              hm = HttpMethod.valueOf(m.toUpperCase());
            } catch (IllegalArgumentException ex) {
              throw new IllegalArgumentException("Unsupported HTTP method in YAML: " + m, ex);
            }
            AuthorizedUrl url = reg.requestMatchers(new AntPathRequestMatcher(pattern, hm.name()));
            applyAccess(url, rule);
          }
        }
      }
      reg.anyRequest().denyAll();
    });
  }

  private void applyAccess(AuthorizedUrl url, SecurityRulesProperties.Rule rule) {
    var access = Objects.requireNonNull(rule.getAccess(), "access is required");

    switch (access) {
      case PERMIT_ALL        -> url.permitAll();
      case AUTHENTICATED     -> url.authenticated();

      case HAS_ROLE          -> url.hasRole(requireExactlyOne(normalizeRoles(rule.getRoles()), "roles"));
      case HAS_ANY_ROLE      -> url.hasAnyRole(requireAtLeastOne(normalizeRoles(rule.getRoles()), "roles"));

      case HAS_AUTHORITY     -> url.hasAuthority(requireExactlyOne(rule.getAuthorities(), "authorities"));
      case HAS_ANY_AUTHORITY -> url.hasAnyAuthority(requireAtLeastOne(rule.getAuthorities(), "authorities"));

      case EXPRESSION -> {
        String expr = rule.getExpression();
        if (!hasText(expr)) {
          throw new IllegalArgumentException("EXPRESSION requires non-empty 'expression'");
        }
        url.access(new WebExpressionAuthorizationManager(expr));
      }
    }
  }

  private static String requireExactlyOne(List<String> list, String field) {
    if (CollectionUtils.isEmpty(list) || list.size() != 1) {
      throw new IllegalArgumentException("Exactly one value required for '" + field + "'");
    }
    return list.get(0);
  }

  private static String[] requireAtLeastOne(List<String> list, String field) {
    if (CollectionUtils.isEmpty(list)) {
      throw new IllegalArgumentException("At least one value required for '" + field + "'");
    }
    return list.toArray(String[]::new);
  }

  private static List<String> normalizeRoles(List<String> roles) {
    if (roles == null) return List.of();
    return roles.stream()
        .filter(Objects::nonNull)
        .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
        .toList();
  }
}

