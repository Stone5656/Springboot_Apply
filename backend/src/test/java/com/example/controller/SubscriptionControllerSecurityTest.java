package com.example.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.example.dto.subscriptions.*;
import com.example.service.SubscriptionService;
import com.example.testbase.AuthPostProcessors;
import com.example.testbase.MvcTestBase;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * SubscriptionController のセキュリティ検証用テスト.
 *
 * - 公開エンドポイント: フォロー/フォロワー一覧
 * - 認証必須エンドポイント: フォロー登録/解除
 */
class SubscriptionControllerSecurityTest extends MvcTestBase {

  @MockBean
  SubscriptionService subscriptionService;

  private static void assertNot401Or403(int status) {
    org.junit.jupiter.api.Assertions.assertTrue(
        status != 401 && status != 403,
        () -> "unexpected 401/403, got " + status);
  }

  // ---------------------------
  // 共有データプロバイダ
  // ---------------------------

  static Stream<Arguments> authedCases() {
    return Stream.of(
        Arguments.of("ADMIN", AuthPostProcessors.admin()),
        Arguments.of("MODERATOR", AuthPostProcessors.moderator()),
        Arguments.of("USER", AuthPostProcessors.user()));
  }

  static Stream<Arguments> allRolesInclAnon() {
    return Stream.of(
        Arguments.of("ADMIN", AuthPostProcessors.admin(), true),
        Arguments.of("MODERATOR", AuthPostProcessors.moderator(), true),
        Arguments.of("USER", AuthPostProcessors.user(), true),
        Arguments.of("ANON", AuthPostProcessors.anon(), true) // 公開は true
    );
  }

  // ========================================================
  // =============== Ⅰ) 公開API（PERMIT_ALL） ===============
  // ========================================================

  @ParameterizedTest(name = "GET /api/users/'{'userId'}'/subscriptions - {0}")
  @MethodSource("allRolesInclAnon")
  void get_subscriptions_by_subscriber_public(
      String who,
      Supplier<RequestPostProcessor> auth,
      boolean allowed) throws Exception {

    UUID userId = UUID.randomUUID();
    var list = List.of(
        SubscriptionResponseDTO.builder()
            .id(UUID.randomUUID())
            .subscriberId(userId)
            .targetId(UUID.randomUUID())
            .build()
    );

    given(subscriptionService.getSubscriptionsBySubscriber(userId)).willReturn(list);

    int s = mvc.perform(
            get("/api/users/{userId}/subscriptions", userId)
                .with(auth.get()))
        .andReturn().getResponse().getStatus();

    if (allowed)
      assertNot401Or403(s);
    else
      org.junit.jupiter.api.Assertions.assertEquals(403, s);
  }

  @ParameterizedTest(name = "GET /api/users/'{'userId'}'/subscribers - {0}")
  @MethodSource("allRolesInclAnon")
  void get_subscriptions_by_target_public(
      String who,
      Supplier<RequestPostProcessor> auth,
      boolean allowed) throws Exception {

    UUID userId = UUID.randomUUID();
    var list = List.of(
        SubscriptionResponseDTO.builder()
            .id(UUID.randomUUID())
            .subscriberId(UUID.randomUUID())
            .targetId(userId)
            .build()
    );

    given(subscriptionService.getSubscriptionsByTarget(userId)).willReturn(list);

    int s = mvc.perform(
            get("/api/users/{userId}/subscribers", userId)
                .with(auth.get()))
        .andReturn().getResponse().getStatus();

    if (allowed)
      assertNot401Or403(s);
    else
      org.junit.jupiter.api.Assertions.assertEquals(403, s);
  }

  // ========================================================
  // ====== Ⅱ) 認証必須（フォロー登録/解除） =================
  // ========================================================

  @ParameterizedTest(name = "POST /api/subscriptions - {0}")
  @MethodSource("authedCases")
  void create_subscription(String who, Supplier<RequestPostProcessor> auth) throws Exception {
    UUID subscriberId = UUID.randomUUID();
    UUID targetId = UUID.randomUUID();

    var req = SubscriptionCreateRequestDTO.builder()
        .subscriberId(subscriberId)
        .targetId(targetId)
        .build();

    var res = SubscriptionResponseDTO.builder()
        .id(UUID.randomUUID())
        .subscriberId(subscriberId)
        .targetId(targetId)
        .build();

    given(subscriptionService.createSubscription(any(SubscriptionCreateRequestDTO.class)))
        .willReturn(res);

    int s = mvc.perform(
            post("/api/subscriptions")
                .with(auth.get())
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
        .andReturn().getResponse().getStatus();

    assertNot401Or403(s);
  }

  @ParameterizedTest(name = "DELETE /api/subscriptions - {0}")
  @MethodSource("authedCases")
  void remove_subscription(String who, Supplier<RequestPostProcessor> auth) throws Exception {
    UUID subscriberId = UUID.randomUUID();
    UUID targetId = UUID.randomUUID();

    var req = SubscriptionRemoveRequestDTO.builder()
        .subscriberId(subscriberId)
        .targetId(targetId)
        .build();

    willDoNothing().given(subscriptionService).removeSubscription(any(SubscriptionRemoveRequestDTO.class));

    int s = mvc.perform(
            delete("/api/subscriptions")
                .with(auth.get())
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
        .andReturn().getResponse().getStatus();

    assertNot401Or403(s);
  }
}
