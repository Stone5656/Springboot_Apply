// src/test/java/com/example/controller/LiveStreamControllerSecurityTest.java
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
class LiveStreamControllerSecurityTest {

  @Autowired
  MockMvc mvc;

  // ==========================================
  // # LiveStreamControllerSecurityTest
  // ## 1. 公開APIの検証（匿名アクセス）
  // ## 2. 認証APIの検証（JWT必須）
  // ## 3. 典型的な異常系（401/403）
  // ==========================================

  private static void assertNot401Or403(int status) {
    org.junit.jupiter.api.Assertions.assertTrue(status != 401 && status != 403,
        () -> "unexpected 401/403, got " + status);
  }

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

    @ParameterizedTest(name = "GET /api/live-streams/'{'id'}' - {0}")
    @MethodSource("com.example.controller.LiveStreamControllerSecurityTest#allRolesInclAnon")
    void get_by_id_public(String who, Supplier<RequestPostProcessor> auth, boolean allowed) throws Exception {
      UUID id = UUID.randomUUID();
      int s = mvc.perform(get("/api/live-streams/{id}", id).with(auth.get()))
                 .andReturn().getResponse().getStatus();
      if (allowed) assertNot401Or403(s);
    }

    @ParameterizedTest(name = "GET /api/live-streams/user/'{'userId'}' - {0}")
    @MethodSource("com.example.controller.LiveStreamControllerSecurityTest#allRolesInclAnon")
    void list_by_user_public(String who, Supplier<RequestPostProcessor> auth, boolean allowed) throws Exception {
      UUID uid = UUID.randomUUID();
      int s = mvc.perform(get("/api/live-streams/user/{userId}", uid).with(auth.get()))
                 .andReturn().getResponse().getStatus();
      if (allowed) assertNot401Or403(s);
    }

    @ParameterizedTest(name = "GET /api/live-streams/user/'{'userId'}'/status - {0}")
    @MethodSource("com.example.controller.LiveStreamControllerSecurityTest#allRolesInclAnon")
    void list_by_user_status_public(String who, Supplier<RequestPostProcessor> auth, boolean allowed) throws Exception {
      UUID uid = UUID.randomUUID();
      int s = mvc.perform(get("/api/live-streams/user/{userId}/status", uid).with(auth.get())
                     .param("status", "LIVE"))
                 .andReturn().getResponse().getStatus();
      if (allowed) assertNot401Or403(s);
    }

    @ParameterizedTest(name = "GET /api/live-streams/status - {0}")
    @MethodSource("com.example.controller.LiveStreamControllerSecurityTest#allRolesInclAnon")
    void list_by_status_public(String who, Supplier<RequestPostProcessor> auth, boolean allowed) throws Exception {
      int s = mvc.perform(get("/api/live-streams/status").with(auth.get())
                     .param("status", "SCHEDULED"))
                 .andReturn().getResponse().getStatus();
      if (allowed) assertNot401Or403(s);
    }

    @ParameterizedTest(name = "GET /api/live-streams/statuses - {0}")
    @MethodSource("com.example.controller.LiveStreamControllerSecurityTest#allRolesInclAnon")
    void list_by_statuses_public(String who, Supplier<RequestPostProcessor> auth, boolean allowed) throws Exception {
      int s = mvc.perform(get("/api/live-streams/statuses").with(auth.get())
                     .param("statuses", "SCHEDULED")
                     .param("statuses", "LIVE"))
                 .andReturn().getResponse().getStatus();
      if (allowed) assertNot401Or403(s);
    }

    @ParameterizedTest(name = "GET /api/live-streams/search - {0}")
    @MethodSource("com.example.controller.LiveStreamControllerSecurityTest#allRolesInclAnon")
    void search_title_status_public(String who, Supplier<RequestPostProcessor> auth, boolean allowed) throws Exception {
      int s = mvc.perform(get("/api/live-streams/search").with(auth.get())
                     .param("title", "foo")
                     .param("status", "LIVE"))
                 .andReturn().getResponse().getStatus();
      if (allowed) assertNot401Or403(s);
    }

    @ParameterizedTest(name = "GET /api/live-streams/key/'{'streamKey'}' - {0}")
    @MethodSource("com.example.controller.LiveStreamControllerSecurityTest#allRolesInclAnon")
    void by_stream_key_public(String who, Supplier<RequestPostProcessor> auth, boolean allowed) throws Exception {
      int s = mvc.perform(get("/api/live-streams/key/{streamKey}", "abc123").with(auth.get()))
                 .andReturn().getResponse().getStatus();
      if (allowed) assertNot401Or403(s);
    }
  }

  @Nested
  class AuthedApis {

    @ParameterizedTest(name = "POST /api/live-streams - {0}")
    @MethodSource("com.example.controller.LiveStreamControllerSecurityTest#authedCases")
    void create_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      int s = mvc.perform(post("/api/live-streams").with(auth.get())
                     .contentType(MediaType.APPLICATION_JSON)
                     .content("{}"))
                 .andReturn().getResponse().getStatus();
      assertNot401Or403(s);
    }

    @Test
    void create_unauth_401() throws Exception {
      mvc.perform(post("/api/live-streams").contentType(MediaType.APPLICATION_JSON).content("{}"))
         .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "PUT /api/live-streams/'{'id'}' - {0}")
    @MethodSource("com.example.controller.LiveStreamControllerSecurityTest#authedCases")
    void update_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      var id = UUID.randomUUID();
      int s = mvc.perform(put("/api/live-streams/{id}", id).with(auth.get())
                     .contentType(MediaType.APPLICATION_JSON)
                     .content("{}"))
                 .andReturn().getResponse().getStatus();
      assertNot401Or403(s);
    }

    @Test
    void update_unauth_401() throws Exception {
      mvc.perform(put("/api/live-streams/{id}", UUID.randomUUID())
              .contentType(MediaType.APPLICATION_JSON)
              .content("{}"))
         .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "PUT /api/live-streams/'{'id'}'/reschedule - {0}")
    @MethodSource("com.example.controller.LiveStreamControllerSecurityTest#authedCases")
    void reschedule_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      var id = UUID.randomUUID();
      int s = mvc.perform(put("/api/live-streams/{id}/reschedule", id).with(auth.get())
                     .contentType(MediaType.APPLICATION_JSON)
                     .content("{}"))
                 .andReturn().getResponse().getStatus();
      assertNot401Or403(s);
    }

    @Test
    void reschedule_unauth_401() throws Exception {
      mvc.perform(put("/api/live-streams/{id}/reschedule", UUID.randomUUID())
              .contentType(MediaType.APPLICATION_JSON)
              .content("{}"))
         .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "POST /api/live-streams/'{'id'}'/open - {0}")
    @MethodSource("com.example.controller.LiveStreamControllerSecurityTest#authedCases")
    void open_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      var id = UUID.randomUUID();
      int s = mvc.perform(post("/api/live-streams/{id}/open", id).with(auth.get()))
                 .andReturn().getResponse().getStatus();
      assertNot401Or403(s);
    }

    @Test
    void open_unauth_401() throws Exception {
      mvc.perform(post("/api/live-streams/{id}/open", UUID.randomUUID()))
         .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "POST /api/live-streams/'{'id'}'/close - {0}")
    @MethodSource("com.example.controller.LiveStreamControllerSecurityTest#authedCases")
    void close_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      var id = UUID.randomUUID();
      int s = mvc.perform(post("/api/live-streams/{id}/close", id).with(auth.get()))
                 .andReturn().getResponse().getStatus();
      assertNot401Or403(s);
    }

    @Test
    void close_unauth_401() throws Exception {
      mvc.perform(post("/api/live-streams/{id}/close", UUID.randomUUID()))
         .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "POST /api/live-streams/'{'id'}'/cancel - {0}")
    @MethodSource("com.example.controller.LiveStreamControllerSecurityTest#authedCases")
    void cancel_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      var id = UUID.randomUUID();
      int s = mvc.perform(post("/api/live-streams/{id}/cancel", id).with(auth.get()))
                 .andReturn().getResponse().getStatus();
      assertNot401Or403(s);
    }

    @Test
    void cancel_unauth_401() throws Exception {
      mvc.perform(post("/api/live-streams/{id}/cancel", UUID.randomUUID()))
         .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "DELETE /api/live-streams/'{'id'}' - {0}")
    @MethodSource("com.example.controller.LiveStreamControllerSecurityTest#authedCases")
    void delete_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      var id = UUID.randomUUID();
      int s = mvc.perform(delete("/api/live-streams/{id}", id).with(auth.get()))
                 .andReturn().getResponse().getStatus();
      assertNot401Or403(s);
    }

    @Test
    void delete_unauth_401() throws Exception {
      mvc.perform(delete("/api/live-streams/{id}", UUID.randomUUID()))
         .andExpect(status().isUnauthorized());
    }
  }
}
