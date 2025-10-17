// src/test/java/com/example/testbase/TestAuth.java
package com.example.testbase;

import java.lang.annotation.*;
import org.springframework.security.test.context.support.WithMockUser;

public class TestAuth {
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @WithMockUser(roles = "ADMIN")
    public @interface AsAdmin {}
    
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @WithMockUser(roles = "USER")
    public @interface AsUser {}
    
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @WithMockUser(roles = "MODERATOR")
    public @interface AsModerator {}
}
