package com.example.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.example.dto.chat.*;
import com.example.service.ChatMessageService;
import com.example.testbase.AuthPostProcessors;
import com.example.testbase.MvcTestBase;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * ChatMessageController のセキュリティ検証用テスト.
 *
 * - 公開エンドポイント: メッセージ取得/検索
 * - 認証必須エンドポイント: メッセージ投稿
 */
class ChatMessageControllerSecurityTest extends MvcTestBase {

  @MockBean
  ChatMessageService chatMessageService;

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

  @ParameterizedTest(name = "GET /api/chat/messages/'{'id'}' - {0}")
  @MethodSource("allRolesInclAnon")
  void get_message_public(String who, Supplier<RequestPostProcessor> auth, boolean allowed)
      throws Exception {

    UUID id = UUID.randomUUID();
    var dto = ChatMessageResponseDTO.builder()
        .id(id)
        .liveStreamId(UUID.randomUUID())
        .userId(UUID.randomUUID())
        .message("hello")
        .build();

    given(chatMessageService.getMessage(id)).willReturn(dto);

    int s = mvc.perform(
            get("/api/chat/messages/{id}", id)
                .with(auth.get()))
        .andReturn().getResponse().getStatus();

    if (allowed)
      assertNot401Or403(s);
    else
      org.junit.jupiter.api.Assertions.assertEquals(403, s);
  }

  @ParameterizedTest(name = "GET /api/chat/messages - {0}")
  @MethodSource("allRolesInclAnon")
  void search_messages_public(String who, Supplier<RequestPostProcessor> auth, boolean allowed)
      throws Exception {

    var page = new PageImpl<>(
        List.of(ChatMessageResponseDTO.builder()
            .id(UUID.randomUUID())
            .liveStreamId(UUID.randomUUID())
            .userId(UUID.randomUUID())
            .message("test")
            .build()),
        PageRequest.of(0, 20),
        1);

    given(chatMessageService.searchMessages(any(ChatMessageSearchRequestDTO.class), any(Pageable.class)))
        .willReturn(page);

    int s = mvc.perform(
            get("/api/chat/messages")
                .param("liveStreamId", UUID.randomUUID().toString()) // 必須パラメータだけダミー指定
                .with(auth.get()))
        .andReturn().getResponse().getStatus();

    if (allowed)
      assertNot401Or403(s);
    else
      org.junit.jupiter.api.Assertions.assertEquals(403, s);
  }

  // ========================================================
  // ============ Ⅱ) 認証必須（メッセージ投稿） ==============
  // ========================================================

  @ParameterizedTest(name = "POST /api/chat/messages - {0}")
  @MethodSource("authedCases")
  void create_message(String who, Supplier<RequestPostProcessor> auth) throws Exception {

    UUID liveStreamId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    var req = ChatMessageCreateRequestDTO.builder()
        .liveStreamId(liveStreamId)
        .userId(userId)
        .message("hello world")
        .build();

    var res = ChatMessageResponseDTO.builder()
        .id(UUID.randomUUID())
        .liveStreamId(liveStreamId)
        .userId(userId)
        .message("hello world")
        .build();

    given(chatMessageService.createMessage(any(ChatMessageCreateRequestDTO.class)))
        .willReturn(res);

    int s = mvc.perform(
            post("/api/chat/messages")
                .with(auth.get())
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
        .andReturn().getResponse().getStatus();

    assertNot401Or403(s);
  }
}
