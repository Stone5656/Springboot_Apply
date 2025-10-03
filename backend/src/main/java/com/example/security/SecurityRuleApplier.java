// com.example.security.SecurityRuleApplier
package com.example.security;

import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.stereotype.Component;

@Component
public class SecurityRuleApplier {

  public void apply(HttpSecurity http,
                    SecurityRulesProperties props,
                    MvcRequestMatcher.Builder mvc) throws Exception {

    http.authorizeHttpRequests(auth -> {
      var rules = props.getRules();
      if (rules == null) return;

      for (int i = 0; i < rules.size(); i++) {
        var r = rules.get(i);

        var methods = (r.getMethods() == null || r.getMethods().isEmpty())
            ? List.<HttpMethod>of()
            : r.getMethods().stream().map(HttpMethod::valueOf).toList();

        var matchers = methods.isEmpty()
            ? List.of(mvc.pattern(r.getPattern()))
            : methods.stream().map(m -> mvc.pattern(m, r.getPattern())).toList();

        for (var m : matchers) {
          switch (String.valueOf(r.getAccess())) {
            case "permitAll"      -> auth.requestMatchers(m).permitAll();
            case "authenticated"  -> auth.requestMatchers(m).authenticated();

            // === 追加: hasRole / hasAnyRole ===
            case "hasRole" -> {
              var roles = normalizeRoles(requireList(r.getAuthorities(), i, r.getPattern()));
              if (roles.size() != 1) {
                throw new IllegalArgumentException(
                  "app.security.rules[" + i + "] pattern=" + r.getPattern()
                  + " requires exactly 1 role for access=hasRole");
              }
              auth.requestMatchers(m).hasRole(roles.get(0)); // ADMIN → ROLE_ADMIN に自動変換
            }
            case "hasAnyRole" -> {
              var roles = normalizeRoles(requireList(r.getAuthorities(), i, r.getPattern()));
              auth.requestMatchers(m).hasAnyRole(roles.toArray(String[]::new));
            }

            // 既存: hasAuthority / hasAnyAuthority
            case "hasAuthority", "hasAnyAuthority" -> {
              var authorities = requireList(r.getAuthorities(), i, r.getPattern());
              auth.requestMatchers(m).hasAnyAuthority(authorities.toArray(String[]::new));
            }

            default -> throw new IllegalArgumentException(
              "Unknown access '" + r.getAccess() + "' at rules[" + i + "] pattern=" + r.getPattern());
          }
        }
      }
    });
  }

  private static List<String> requireList(List<String> list, int idx, String pattern) {
    if (list == null || list.isEmpty() || list.stream().anyMatch(Objects::isNull)) {
      throw new IllegalArgumentException(
        "app.security.rules[" + idx + "] pattern=" + pattern + " requires non-empty 'authorities'");
    }
    return list;
  }

  /**
   * hasRole/hasAnyRole 用に ROLE_ 接頭辞を付けず記述してもらう前提だが、
   * 誤って ROLE_ を書かれても動くように安全に剥がしておく。
   */
  private static List<String> normalizeRoles(List<String> roles) {
    return roles.stream()
      .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
      .toList();
  }
}
