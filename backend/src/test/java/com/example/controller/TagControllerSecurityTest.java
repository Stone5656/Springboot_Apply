package com.example.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.example.dto.tags.*;
import com.example.service.TagService;
import com.example.testbase.AuthPostProcessors;
import com.example.testbase.MvcTestBase;

import java.util.*;
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
 * TagController のセキュリティ検証用テスト
 *
 * CategoryControllerSecurityTest と同じテストパターンに揃えています。
 */
class TagControllerSecurityTest extends MvcTestBase {

  @MockBean
  TagService tagService;

  private static void assertNot401Or403(int status) {
    org.junit.jupiter.api.Assertions.assertTrue(status != 401 && status != 403,
        () -> "unexpected 401/403, got " + status);
  }

  // ---------------------------
  // 共有データプロバイダ（Category と同じ）
  // ---------------------------
  static Stream<Arguments> authedCases() {
    return Stream.of(Arguments.of("ADMIN", AuthPostProcessors.admin()),
        Arguments.of("MODERATOR", AuthPostProcessors.moderator()),
        Arguments.of("USER", AuthPostProcessors.user()));
  }

  static Stream<Arguments> allRolesInclAnon() {
    return Stream.of(Arguments.of("ADMIN", AuthPostProcessors.admin(), true),
        Arguments.of("MODERATOR", AuthPostProcessors.moderator(), true),
        Arguments.of("USER", AuthPostProcessors.user(), true),
        Arguments.of("ANON", AuthPostProcessors.anon(), true) // 公開は true
    );
  }

  // ========================================================
  // =============== Ⅰ) 公開API（PERMIT_ALL） ===============
  // ========================================================

  @ParameterizedTest(name = "GET /api/tags - {0}")
  @MethodSource("allRolesInclAnon")
  void search_tags_public(String who, Supplier<RequestPostProcessor> auth, boolean allowed)
      throws Exception {
    var page =
        new PageImpl<>(List.of(TagResponseDTO.builder().id(UUID.randomUUID()).name("Tech").build()),
            PageRequest.of(0, 20), 1);

    given(tagService.searchTags(any(TagSearchRequestDTO.class), any(Pageable.class)))
        .willReturn(page);

    int s = mvc.perform(get("/api/tags").with(auth.get())).andReturn().getResponse().getStatus();

    if (allowed)
      assertNot401Or403(s);
    else
      org.junit.jupiter.api.Assertions.assertEquals(403, s);
  }

  @ParameterizedTest(name = "GET /api/tags/'{'id'}' - {0}")
  @MethodSource("allRolesInclAnon")
  void get_tag_public(String who, Supplier<RequestPostProcessor> auth, boolean allowed)
      throws Exception {
    UUID id = UUID.randomUUID();
    var dto = TagResponseDTO.builder().id(id).name("Music").build();

    given(tagService.getTag(id)).willReturn(dto);

    int s =
        mvc.perform(get("/api/tags/{id}", id).with(auth.get())).andReturn().getResponse().getStatus();

    if (allowed)
      assertNot401Or403(s);
    else
      org.junit.jupiter.api.Assertions.assertEquals(403, s);
  }

  // ========================================================
  // ====== Ⅱ) 認証必須（Video / LiveStream ひもづけ） ======
  // ========================================================

  // ---------- Video × Tag ----------

  @ParameterizedTest(name = "POST /api/videos/'{'videoId'}'/tags - {0}")
  @MethodSource("authedCases")
  void add_video_tags(String who, Supplier<RequestPostProcessor> auth) throws Exception {
    var videoId = UUID.randomUUID();
    var body = om.writeValueAsString(List.of(UUID.randomUUID()));

    willDoNothing().given(tagService).addTagsToVideo(eq(videoId), anyList());

    int s = mvc
        .perform(post("/api/videos/{videoId}/tags", videoId).with(auth.get())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andReturn().getResponse().getStatus();

    assertNot401Or403(s);
  }

  @ParameterizedTest(name = "PUT /api/videos/'{'videoId'}'/tags - {0}")
  @MethodSource("authedCases")
  void replace_video_tags(String who, Supplier<RequestPostProcessor> auth) throws Exception {
    var videoId = UUID.randomUUID();
    var body = om.writeValueAsString(List.of(UUID.randomUUID()));

    willDoNothing().given(tagService).replaceVideoTag(eq(videoId), anyList());

    int s = mvc
        .perform(put("/api/videos/{videoId}/tags", videoId).with(auth.get())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andReturn().getResponse().getStatus();

    assertNot401Or403(s);
  }

  @ParameterizedTest(name = "DELETE /api/videos/'{'videoId'}'/tags/'{'tagId'}' - {0}")
  @MethodSource("authedCases")
  void remove_video_tag(String who, Supplier<RequestPostProcessor> auth) throws Exception {
    var videoId = UUID.randomUUID();
    var tagId = UUID.randomUUID();

    willDoNothing().given(tagService).removeTagFromVideo(videoId, tagId);

    int s =
        mvc.perform(delete("/api/videos/{videoId}/tags/{tagId}", videoId, tagId).with(auth.get())
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
            .andReturn().getResponse().getStatus();

    assertNot401Or403(s);
  }

  // ---------- LiveStream × Tag ----------

  @ParameterizedTest(name = "POST /api/livestreams/'{'liveStreamId'}'/tags - {0}")
  @MethodSource("authedCases")
  void add_livestream_tags(String who, Supplier<RequestPostProcessor> auth) throws Exception {
    var liveStreamId = UUID.randomUUID();
    var body = om.writeValueAsString(List.of(UUID.randomUUID()));

    willDoNothing().given(tagService).addTagsToLiveStream(eq(liveStreamId), anyList());

    int s = mvc
        .perform(post("/api/livestreams/{liveStreamId}/tags", liveStreamId).with(auth.get())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andReturn().getResponse().getStatus();

    assertNot401Or403(s);
  }

  @ParameterizedTest(name = "PUT /api/livestreams/'{'liveStreamId'}'/tags - {0}")
  @MethodSource("authedCases")
  void replace_livestream_tags(String who, Supplier<RequestPostProcessor> auth) throws Exception {
    var liveStreamId = UUID.randomUUID();
    var body = om.writeValueAsString(List.of(UUID.randomUUID()));

    willDoNothing().given(tagService).replaceLiveStreamTag(eq(liveStreamId), anyList());

    int s = mvc
        .perform(put("/api/livestreams/{liveStreamId}/tags", liveStreamId).with(auth.get())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andReturn().getResponse().getStatus();

    assertNot401Or403(s);
  }

  @ParameterizedTest(
      name = "DELETE /api/livestreams/'{'liveStreamId'}'/tags/'{'tagId'}' - {0}")
  @MethodSource("authedCases")
  void remove_livestream_tag(String who, Supplier<RequestPostProcessor> auth) throws Exception {
    var liveStreamId = UUID.randomUUID();
    var tagId = UUID.randomUUID();

    willDoNothing().given(tagService).removeTagFromLiveStream(liveStreamId, tagId);

    int s = mvc
        .perform(delete("/api/livestreams/{liveStreamId}/tags/{tagId}", liveStreamId, tagId)
            .with(auth.get()).with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andReturn().getResponse().getStatus();

    assertNot401Or403(s);
  }

  // ========================================================
  // ============= Ⅲ) 管理者専用（hasRole('ADMIN')） =========
  // ========================================================

  @ParameterizedTest(name = "POST /api/tags - {0}")
  @MethodSource("authedCases")
  void create_tag_role_matrix(String who, Supplier<RequestPostProcessor> auth) throws Exception {
    var req = TagCreateRequestDTO.builder().name("Tech").build();
    var res = TagResponseDTO.builder().id(UUID.randomUUID()).name("Tech").build();

    given(tagService.createTag(any(TagCreateRequestDTO.class))).willReturn(res);

    int s = mvc
        .perform(post("/api/tags").with(auth.get())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(req)))
        .andReturn().getResponse().getStatus();

    if ("ADMIN".equals(who))
      assertNot401Or403(s);
    else
      org.junit.jupiter.api.Assertions.assertEquals(403, s);
  }

  @ParameterizedTest(name = "PUT /api/tags/'{'id'}' - {0}")
  @MethodSource("authedCases")
  void update_tag_role_matrix(String who, Supplier<RequestPostProcessor> auth) throws Exception {
    UUID id = UUID.randomUUID();
    var req = TagUpdateRequestDTO.builder().name("News").build();
    var res = TagResponseDTO.builder().id(id).name("News").build();

    given(tagService.updateTag(eq(id), any(TagUpdateRequestDTO.class))).willReturn(res);

    int s = mvc
        .perform(put("/api/tags/{id}", id).with(auth.get())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(req)))
        .andReturn().getResponse().getStatus();

    if ("ADMIN".equals(who))
      assertNot401Or403(s);
    else
      org.junit.jupiter.api.Assertions.assertEquals(403, s);
  }

  @ParameterizedTest(name = "DELETE /api/tags/'{'id'}' - {0}")
  @MethodSource("authedCases")
  void delete_tag_role_matrix(String who, Supplier<RequestPostProcessor> auth) throws Exception {
    UUID id = UUID.randomUUID();
    willDoNothing().given(tagService).deleteTag(id);

    int s = mvc
        .perform(delete("/api/tags/{id}", id).with(auth.get())
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andReturn().getResponse().getStatus();

    if ("ADMIN".equals(who))
      assertNot401Or403(s);
    else
      org.junit.jupiter.api.Assertions.assertEquals(403, s);
  }
}
