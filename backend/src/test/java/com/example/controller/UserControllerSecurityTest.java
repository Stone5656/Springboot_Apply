// src/test/java/com/example/controller/UserControllerSecurityTest.java
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
class UserControllerSecurityTest {

  @Autowired MockMvc mvc;

  /* ========== 共通ヘルパ：許可側は “≠401/403” を検証 ========== */
  private static void assertNot401Or403(int status) throws Exception {
    if (status == 401 || status == 403) {
      throw new AssertionError("expected not to be 401/403, but was: " + status);
    }
  }

  /* ========== Auth セクション ========== */
  @Nested
  class Auth {

    static Stream<Arguments> registerCases() {
      return Stream.of(
          Arguments.of("ANON",  AuthPostProcessors.anon(),  true),  // 公開OK → 401/403 ではない
          Arguments.of("USER",  AuthPostProcessors.user(),  true),
          Arguments.of("MOD",   AuthPostProcessors.moderator(), true),
          Arguments.of("ADMIN", AuthPostProcessors.admin(), true)
      );
    }

    @ParameterizedTest(name = "POST /api/users/register - {0}")
    @MethodSource("registerCases")
    void register_roleMatrix(String who, Supplier<RequestPostProcessor> auth, boolean allowed) throws Exception {
      var req = post("/api/users/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{}"); // ボディは最低限（業務結果ではなく認可の挙動のみ検証）

      var res = (who.equals("ANON")) ? mvc.perform(req) : mvc.perform(req.with(auth.get()));
      var result = res.andReturn().getResponse().getStatus();

      if (allowed) {
        assertNot401Or403(result);
      } else {
        // 今回は全ロール許可なので到達しない分岐
        if (who.equals("ANON")) { org.junit.jupiter.api.Assertions.assertEquals(401, result); }
        else { org.junit.jupiter.api.Assertions.assertEquals(403, result); }
      }
    }

    static Stream<Arguments> loginCases() {
      return registerCases(); // login も公開
    }

    @ParameterizedTest(name = "POST /api/users/login - {0}")
    @MethodSource("loginCases")
    void login_roleMatrix(String who, Supplier<RequestPostProcessor> auth, boolean allowed) throws Exception {
      var req = post("/api/users/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{}");
      var res = (who.equals("ANON")) ? mvc.perform(req) : mvc.perform(req.with(auth.get()));
      var result = res.andReturn().getResponse().getStatus();
      if (allowed) { assertNot401Or403(result); } else {
        if (who.equals("ANON")) { org.junit.jupiter.api.Assertions.assertEquals(401, result); }
        else { org.junit.jupiter.api.Assertions.assertEquals(403, result); }
      }
    }
  }

  /* ========== Me セクション（要認証） ========== */
  @Nested
  class Me {

    static Stream<Arguments> authedCases() {
      return Stream.of(
          Arguments.of("USER",  AuthPostProcessors.user()),
          Arguments.of("MOD",   AuthPostProcessors.moderator()),
          Arguments.of("ADMIN", AuthPostProcessors.admin())
      );
    }

    @ParameterizedTest(name = "GET /api/users/me - {0}")
    @MethodSource("authedCases")
    void me_get_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      var res = mvc.perform(get("/api/users/me").with(auth.get()))
                   .andReturn().getResponse().getStatus();
      assertNot401Or403(res);
    }

    @Test
    void me_get_unauth_is401() throws Exception {
      mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "PUT /api/users/me - {0}")
    @MethodSource("authedCases")
    void me_put_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      var res = mvc.perform(put("/api/users/me").with(auth.get())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content("{}"))
                   .andReturn().getResponse().getStatus();
      assertNot401Or403(res);
    }

    @Test
    void me_put_unauth_is401() throws Exception {
      mvc.perform(put("/api/users/me")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{}"))
         .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "DELETE /api/users/me - {0}")
    @MethodSource("authedCases")
    void me_delete_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      var res = mvc.perform(delete("/api/users/me").with(auth.get()))
                   .andReturn().getResponse().getStatus();
      assertNot401Or403(res);
    }

    @Test
    void me_delete_unauth_is401() throws Exception {
      mvc.perform(delete("/api/users/me")).andExpect(status().isUnauthorized());
    }
  }

  /* ========== Admin-only セクション（ADMINのみ許可） ========== */
  @Nested
  class AdminOnly {
    static final String PATH = "/api/users/{id}/restore";

    @Test
    void admin_restore_ok_not401or403() throws Exception {
      var id = UUID.randomUUID().toString();
      int status = mvc.perform(put(PATH, id).with(AuthPostProcessors.admin().get()))
                      .andReturn().getResponse().getStatus();
      assertNot401Or403(status); // 200/404等、いずれにせよ 401/403 でなければOK
    }

    @Test
    void user_restore_forbidden_403() throws Exception {
      var id = UUID.randomUUID().toString();
      mvc.perform(put(PATH, id).with(AuthPostProcessors.user().get()))
         .andExpect(status().isForbidden());
    }

    @Test
    void mod_restore_forbidden_403() throws Exception {
      var id = UUID.randomUUID().toString();
      mvc.perform(put(PATH, id).with(AuthPostProcessors.moderator().get()))
         .andExpect(status().isForbidden());
    }

    @Test
    void anon_restore_unauthorized_401() throws Exception {
      var id = UUID.randomUUID().toString();
      mvc.perform(put(PATH, id)).andExpect(status().isUnauthorized());
    }
  }

  /* ========== Account Security（要認証） ========== */
  @Nested
  class AccountSecurity {

    static Stream<Arguments> authedCases() {
      return Me.authedCases();
    }

    @ParameterizedTest(name = "POST /api/users/me/password - {0}")
    @MethodSource("authedCases")
    void password_change_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      int s = mvc.perform(post("/api/users/me/password").with(auth.get())
                     .contentType(MediaType.APPLICATION_JSON)
                     .content("{}"))
                 .andReturn().getResponse().getStatus();
      assertNot401Or403(s);
    }

    @Test
    void password_change_unauth_401() throws Exception {
      mvc.perform(post("/api/users/me/password")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{}"))
         .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "POST /api/users/me/email - {0}")
    @MethodSource("authedCases")
    void email_change_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      int s = mvc.perform(post("/api/users/me/email").with(auth.get())
                     .contentType(MediaType.APPLICATION_JSON)
                     .content("{}"))
                 .andReturn().getResponse().getStatus();
      assertNot401Or403(s);
    }

    @Test
    void email_change_unauth_401() throws Exception {
      mvc.perform(post("/api/users/me/email")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{}"))
         .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "POST /api/users/me/remember-token - {0}")
    @MethodSource("authedCases")
    void remember_issue_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      int s = mvc.perform(post("/api/users/me/remember-token").with(auth.get())
                     .param("duration", "PT15M"))
                 .andReturn().getResponse().getStatus();
      assertNot401Or403(s);
    }

    @Test
    void remember_issue_unauth_401() throws Exception {
      mvc.perform(post("/api/users/me/remember-token").param("duration", "PT15M"))
         .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "GET /api/users/me/remember-token/verify - {0}")
    @MethodSource("authedCases")
    void remember_verify_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      int s = mvc.perform(get("/api/users/me/remember-token/verify").with(auth.get())
                     .param("token", "dummy"))
                 .andReturn().getResponse().getStatus();
      assertNot401Or403(s);
    }

    @Test
    void remember_verify_unauth_401() throws Exception {
      mvc.perform(get("/api/users/me/remember-token/verify").param("token", "dummy"))
         .andExpect(status().isUnauthorized());
    }
  }

  /* ========== Preferences / Contact（要認証） ========== */
  @Nested
  class PreferencesContact {

    static Stream<Arguments> authedCases() { return Me.authedCases(); }

    @ParameterizedTest(name = "PUT /api/users/me/preferences - {0}")
    @MethodSource("authedCases")
    void preferences_put_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      int s = mvc.perform(put("/api/users/me/preferences").with(auth.get())
                     .contentType(MediaType.APPLICATION_JSON)
                     .content("{}"))
                 .andReturn().getResponse().getStatus();
      assertNot401Or403(s);
    }

    @Test
    void preferences_put_unauth_401() throws Exception {
      mvc.perform(put("/api/users/me/preferences")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{}"))
         .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "PUT /api/users/me/phone - {0}")
    @MethodSource("authedCases")
    void phone_put_authed(String who, Supplier<RequestPostProcessor> auth) throws Exception {
      int s = mvc.perform(put("/api/users/me/phone").with(auth.get())
                     .contentType(MediaType.APPLICATION_JSON)
                     .content("{}"))
                 .andReturn().getResponse().getStatus();
      assertNot401Or403(s);
    }

    @Test
    void phone_put_unauth_401() throws Exception {
      mvc.perform(put("/api/users/me/phone")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{}"))
         .andExpect(status().isUnauthorized());
    }
  }
}
