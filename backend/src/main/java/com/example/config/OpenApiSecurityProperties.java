// src/main/java/com/example/config/OpenApiSecurityProperties.java
package com.example.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.openapi")
public class OpenApiSecurityProperties {
    /** 認証不要としてSwagger上で扱いたいパス(ant形式可) */
    private List<String> anonymousPaths = new ArrayList<>();

    public List<String> getAnonymousPaths() { return anonymousPaths; }
    public void setAnonymousPaths(List<String> anonymousPaths) { this.anonymousPaths = anonymousPaths; }
}
