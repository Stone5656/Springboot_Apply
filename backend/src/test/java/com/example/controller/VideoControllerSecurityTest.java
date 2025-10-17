// src/test/java/com/example/controller/VideoControllerSecurityTest.java
package com.example.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.testbase.AuthPostProcessors;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
class VideoControllerSecurityTest {

  @Autowired
  MockMvc mvc;

  // ==========================================
  // # VideoControllerSecurityTest
  // ## 1. 公開APIの検証（匿名アクセス）
  // ## 2. 認証APIの検証（JWT必須）
  // ## 3. 管理者専用APIの検証（HAS_ROLE）
  // ## 4. 典型的な異常系（401/403）
  // ==========================================

  private static void assertNot401Or403(int status) {
    org.junit.jupiter.api.Assertions.assertTrue(status != 401 && status != 403,
        () -> "unexpected 401/403, got " + status);
  }

  // ---- roles ----
  static Stream<Arguments> authedCases() {
    return Stream.of(
        Arguments.of("ADMIN", AuthPostProcessors.admin()),
        Arguments.of("MODERATOR", AuthPostProcessors.moderator()),
        Arguments.of("USER", AuthPostProcessors.user())
    );
  }

  static Stream<Arguments> allRolesInclAnon() {
    return Stream.of(
        Arguments.of("ADMIN", AuthPostProcessors.admin(), true),
        Arguments.of("MODERATOR", AuthPostProcessors.moderator(), true),
        Arguments.of("USER", AuthPostProcessors.user(), true),
        Arguments.of("ANON", AuthPostProcessors.anon(), true) // 公開は true
    );
  }

  @Nested
  class PublicApis {

    @ParameterizedTest(name = "GET /api/videos/{id} - {0}")
    @MethodSource("com.example.controller.VideoControllerSecurityTest#allRolesInclAnon")
    void get_by_id_public(String who, Supplier<RequestPostProcessor> auth, boolean allowed) throws Exception {
      var id = UUID.randomUUID();
      int s = mvc.perform(get("/api/videos/{id}", id).with(auth.get()))
                 .andReturn().getResponse().getStatus();
      if (allowed) assertNot401Or403(s); else org.junit.jupiter.api.Assertions.fail();
    }

    @ParameterizedTest(name = "GET /api/videos (search) - {0}")
    @MethodSource("com.example.controller.VideoControllerSecurityTest#allRolesInclAnon")
    void search_public(String who, Supplier<RequestPostProcessor> auth, boolean allowed) throws Exception {
      int s = mvc.perform(get("/api/videos").with(auth.get()))
                 .andReturn().getResponse().getStatus();
      if (allowed) assertNot401Or403(s);
    }

    @ParameterizedTest(name = "GET /api/videos/user/{userId} - {0}")
    @MethodSource("com.example.controller.VideoControllerSecurityTest#allRolesInclAnon")
    void public_by_user(String who, Supplier<RequestPostProcessor> auth, boolean allowed) throws Exception {
      var uid = UUID.randomUUID();
      int s = mvc.perform(get("/api/videos/user/{userId}", uid).with(auth.get()))
                 .andReturn().getResponse().getStatus();
      if (allowed) assertNot401Or403(s);
    }

    @ParameterizedTest(name = "GET /api/videos/popular - {0}")
    @MethodSource("com.example.controller.VideoControllerSecurityTest#allRolesInclAnon")
    void popular_public(String who, Supplier<RequestPostProcessor> auth, boolean allowed) throws Exception {
      int s = mvc.perform(get("/api/videos/popular").with(auth.get()))
                 .andReturn().getResponse().getStatus();
      if (allowed) assertNot401Or403(s);
    }

    @ParameterizedTest(name = "GET /api/videos/recent - {0}")
    @MethodSource("com.example.controller.VideoControllerSecurityTest#allRolesInclAnon")
    void recent_public(String who, Supplier<RequestPostProcessor> auth, boolean allowed) throws Exception {
      int s = mvc.perform(get("/api/videos/recent").with(auth.get()))
                 .andReturn().getResponse().getStatus();
      if (allowed) assertNot401Or403(s);
    }

    @ParameterizedTest(name = "PATCH /api/videos/{id}/views - {0}")
    @MethodSource("com.example.controller.VideoControllerSecurityTest#allRolesInclAnon")
    void views_public(String who, Supplier<RequestPostProcessor> auth, boolean allowed) throws Exception {
      var id = UUID.randomUUID();
      int s = mvc.perform(patch("/api/videos/{id}/views", id).with(auth.get()))
                 .andReturn().getResponse().getStatus();
      if (allowed) assertNot401Or403(s);
    }
  }

  @Nested
  class AuthedApis {

    @ParameterizedTest(name = "GET /api/videos/my - {0}")
    @MethodSource("com.example.controller.VideoControllerSecurityTest#authedCases")
    void my_list_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      int s = mvc.perform(get("/api/videos/my").with(auth.get()))
                 .andReturn().getResponse().getStatus();
      assertNot401Or403(s);
    }

    @Test
    void my_list_unauth_401() throws Exception {
      mvc.perform(get("/api/videos/my"))
         .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "POST /api/videos - {0}")
    @MethodSource("com.example.controller.VideoControllerSecurityTest#authedCases")
    void create_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      int s = mvc.perform(post("/api/videos").with(auth.get())
                     .contentType(MediaType.APPLICATION_JSON)
                     .content("{}"))
                 .andReturn().getResponse().getStatus();
      assertNot401Or403(s);
    }

    @Test
    void create_unauth_401() throws Exception {
      mvc.perform(post("/api/videos").contentType(MediaType.APPLICATION_JSON).content("{}"))
         .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "PUT /api/videos/{id} - {0}")
    @MethodSource("com.example.controller.VideoControllerSecurityTest#authedCases")
    void update_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      var id = UUID.randomUUID();
      int s = mvc.perform(put("/api/videos/{id}", id).with(auth.get())
                     .contentType(MediaType.APPLICATION_JSON)
                     .content("{}"))
                 .andReturn().getResponse().getStatus();
      assertNot401Or403(s);
    }

    @Test
    void update_unauth_401() throws Exception {
      mvc.perform(put("/api/videos/{id}", UUID.randomUUID())
              .contentType(MediaType.APPLICATION_JSON)
              .content("{}"))
         .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "DELETE /api/videos/{id} - {0}")
    @MethodSource("com.example.controller.VideoControllerSecurityTest#authedCases")
    void delete_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      var id = UUID.randomUUID();
      int s = mvc.perform(delete("/api/videos/{id}", id).with(auth.get()))
                 .andReturn().getResponse().getStatus();
      assertNot401Or403(s);
    }

    @Test
    void delete_unauth_401() throws Exception {
      mvc.perform(delete("/api/videos/{id}", UUID.randomUUID()))
         .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "PATCH /api/videos/{id}/publish - {0}")
    @MethodSource("com.example.controller.VideoControllerSecurityTest#authedCases")
    void publish_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      var id = UUID.randomUUID();
      int s = mvc.perform(patch("/api/videos/{id}/publish", id).with(auth.get()))
                 .andReturn().getResponse().getStatus();
      assertNot401Or403(s);
    }

    @Test
    void publish_unauth_401() throws Exception {
      mvc.perform(patch("/api/videos/{id}/publish", UUID.randomUUID()))
         .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "PATCH /api/videos/{id}/unpublish - {0}")
    @MethodSource("com.example.controller.VideoControllerSecurityTest#authedCases")
    void unpublish_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      var id = UUID.randomUUID();
      int s = mvc.perform(patch("/api/videos/{id}/unpublish", id).with(auth.get()))
                 .andReturn().getResponse().getStatus();
      assertNot401Or403(s);
    }

    @Test
    void unpublish_unauth_401() throws Exception {
      mvc.perform(patch("/api/videos/{id}/unpublish", UUID.randomUUID()))
         .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  class AdminApis {

    @ParameterizedTest(name = "POST /api/videos/{id}/restore - ADMIN only ({0})")
    @MethodSource("com.example.controller.VideoControllerSecurityTest#authedCases")
    void restore_role_matrix(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      var id = UUID.randomUUID();
      int s = mvc.perform(post("/api/videos/{id}/restore", id).with(auth.get()))
                 .andReturn().getResponse().getStatus();
      if ("ADMIN".equals(who)) assertNot401Or403(s);
      else org.junit.jupiter.api.Assertions.assertEquals(403, s);
    }

    @Test
    void restore_unauth_401() throws Exception {
      mvc.perform(post("/api/videos/{id}/restore", UUID.randomUUID()))
         .andExpect(status().isUnauthorized());
    }
  }
}
