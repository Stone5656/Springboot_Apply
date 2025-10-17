// src/test/java/com/example/testbase/AuthPostProcessors.java
package com.example.testbase;

import java.util.function.Supplier;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public final class AuthPostProcessors {
  private AuthPostProcessors() {}

  public static Supplier<RequestPostProcessor> admin() {
    return () -> SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN");
  }
  public static Supplier<RequestPostProcessor> moderator() {
    return () -> SecurityMockMvcRequestPostProcessors.user("mod").roles("MODERATOR");
  }
  public static Supplier<RequestPostProcessor> user() {
    return () -> SecurityMockMvcRequestPostProcessors.user("user").roles("USER");
  }
  public static Supplier<RequestPostProcessor> anon() {
    // 未認証 = 何も付けない。呼び出し側で with(...) しない。
    return () -> r -> r;
  }
}
