// src/test/java/com/example/AdminApiTests.java
package com.example;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
// Spring Security の RequestPostProcessor（user() を使うなら）
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// src/test/java/com/example/AdminApiTests.java
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
@Import(AdminApiTests.TestAdminPingController.class) // ← 確実に登録
class AdminApiTests {

  @RestController
  @RequestMapping("/api/admin")
  static class TestAdminPingController {
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
      return ResponseEntity.ok("pong");
    }
  }

  @Autowired MockMvc mvc;

  @Test
  void admin_ping_is_403_for_user() throws Exception {
    mvc.perform(get("/api/admin/ping")
          .with(user("u").authorities(new SimpleGrantedAuthority("USER"))))           // ロール=USER
       .andExpect(status().isForbidden());
  }

  @Test
  void admin_ping_is_200_for_admin() throws Exception {
    mvc.perform(get("/api/admin/ping")
          .with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))      // ロール=ADMIN
       .andExpect(status().isOk())
       .andExpect(content().string("pong"));
  }
}
