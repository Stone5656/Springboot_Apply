// src/main/java/com/example/security/SecurityRulesProperties.java
package com.example.security;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public class SecurityRulesProperties {

    private List<Rule> rules = new ArrayList<>();

    public List<Rule> getRules() {
        return rules;
    }
    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public static class Rule {
        private String pattern;
        private List<String> methods;      // 省略可（未指定なら全メソッド）
        private String access;             // permitAll | authenticated | hasAuthority | hasAnyAuthority
        private List<String> authorities;  // has(Any)Authority 用

        public Rule() {} // ConfigurationProperties のためにデフォルトコンストラクタが必要

        public String getPattern() {
            return pattern;
        }
        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public List<String> getMethods() {
            return methods;
        }
        public void setMethods(List<String> methods) {
            this.methods = methods;
        }

        public String getAccess() {
            return access;
        }
        public void setAccess(String access) {
            this.access = access;
        }

        public List<String> getAuthorities() {
            return authorities;
        }
        public void setAuthorities(List<String> authorities) {
            this.authorities = authorities;
        }
    }
}
