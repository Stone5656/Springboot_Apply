// src/main/java/com/example/security/SecurityRulesProperties.java
package com.example.security;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public class SecurityRulesProperties {

  private List<Rule> rules = new ArrayList<>();
  public List<Rule> getRules() { return rules; }
  public void setRules(List<Rule> rules) { this.rules = rules; }

  public static class Rule {
    private String pattern;
    private List<String> methods;
    private Access access;
    private List<String> authorities;
    private List<String> roles;
    private String expression;

    public enum Access {
      PERMIT_ALL, AUTHENTICATED,
      HAS_ROLE, HAS_ANY_ROLE,
      HAS_AUTHORITY, HAS_ANY_AUTHORITY,
      EXPRESSION
    }

    public Rule() {}

    // getters/setters（省略可）
    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }
    public List<String> getMethods() { return methods; }
    public void setMethods(List<String> methods) { this.methods = methods; }
    public Access getAccess() { return access; }
    public void setAccess(Access access) { this.access = access; }
    public List<String> getAuthorities() { return authorities; }
    public void setAuthorities(List<String> authorities) { this.authorities = authorities; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public String getExpression() { return expression; }
    public void setExpression(String expression) { this.expression = expression; }
  }
}
