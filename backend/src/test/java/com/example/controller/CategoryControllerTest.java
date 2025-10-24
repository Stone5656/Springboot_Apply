package com.example.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.example.dto.categories.*;
import com.example.service.CategoryService;
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

class CategoryControllerSecurityTest extends MvcTestBase {

  @MockBean
  CategoryService categoryService;

  private static void assertNot401Or403(int status) {
    org.junit.jupiter.api.Assertions.assertTrue(status != 401 && status != 403,
        () -> "unexpected 401/403, got " + status);
  }

  // ---------------------------
  // 共有データプロバイダ（他コントローラと統一）
  // ---------------------------
  static Stream<Arguments> authedCases() {
    return Stream.of(
        Arguments.of("ADMIN",     AuthPostProcessors.admin()),
        Arguments.of("MODERATOR", AuthPostProcessors.moderator()),
        Arguments.of("USER",      AuthPostProcessors.user())
    );
  }

  static Stream<Arguments> allRolesInclAnon() {
    return Stream.of(
        Arguments.of("ADMIN",     AuthPostProcessors.admin(),     true),
        Arguments.of("MODERATOR", AuthPostProcessors.moderator(), true),
        Arguments.of("USER",      AuthPostProcessors.user(),      true),
        Arguments.of("ANON",      AuthPostProcessors.anon(),      true) // 公開は true
    );
  }

  // ========================================================
  // =============== Ⅰ) 公開API（PERMIT_ALL） ===============
  // ========================================================

  @ParameterizedTest(name = "GET /api/categories - {0}")
  @MethodSource("allRolesInclAnon")
  void search_categories_public(String who, Supplier<RequestPostProcessor> auth, boolean allowed) throws Exception {
    var page = new PageImpl<>(
        List.of(CategoryResponseDTO.builder()
            .id(UUID.randomUUID())
            .name("Tech")
            .build()),
        PageRequest.of(0, 20),
        1);

    given(categoryService.searchCategories(any(CategorySearchRequestDTO.class), any(Pageable.class)))
        .willReturn(page);

    int s = mvc.perform(get("/api/categories").with(auth.get()))
        .andReturn().getResponse().getStatus();

    if (allowed) assertNot401Or403(s); else org.junit.jupiter.api.Assertions.assertEquals(403, s);
  }

  @ParameterizedTest(name = "GET /api/categories/'{'id'}' - {0}")
  @MethodSource("allRolesInclAnon")
  void get_category_public(String who, Supplier<RequestPostProcessor> auth, boolean allowed) throws Exception {
    UUID id  = UUID.randomUUID();
    var dto = CategoryResponseDTO.builder().id(id).name("Music").build();

    given(categoryService.getCategory(id)).willReturn(dto);

    int s = mvc.perform(get("/api/categories/{id}", id).with(auth.get()))
        .andReturn().getResponse().getStatus();

    if (allowed) assertNot401Or403(s); else org.junit.jupiter.api.Assertions.assertEquals(403, s);
  }

  // ========================================================
  // ====== Ⅱ) 認証必須（Video / LiveStream ひもづけ） ======
  // ========================================================

  @ParameterizedTest(name = "POST /api/videos/'{'videoId'}'/categories - {0}")
  @MethodSource("authedCases")
  void add_video_categories(String who, Supplier<RequestPostProcessor> auth) throws Exception {
    var videoId = UUID.randomUUID();
    var body    = om.writeValueAsString(List.of(UUID.randomUUID()));

    willDoNothing().given(categoryService).addCategoriesToVideo(eq(videoId), anyList());

    int s = mvc.perform(post("/api/videos/{videoId}/categories", videoId)
            .with(auth.get())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andReturn().getResponse().getStatus();

    assertNot401Or403(s);
  }

  @ParameterizedTest(name = "PUT /api/videos/'{'videoId'}'/categories - {0}")
  @MethodSource("authedCases")
  void replace_video_categories(String who, Supplier<RequestPostProcessor> auth) throws Exception {
    var videoId = UUID.randomUUID();
    var body    = om.writeValueAsString(List.of(UUID.randomUUID()));

    willDoNothing().given(categoryService).replaceVideoCategories(eq(videoId), anyList());

    int s = mvc.perform(put("/api/videos/{videoId}/categories", videoId)
            .with(auth.get())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andReturn().getResponse().getStatus();

    assertNot401Or403(s);
  }

  @ParameterizedTest(name = "DELETE /api/videos/'{'videoId'}'/categories/'{'categoryId'}' - {0}")
  @MethodSource("authedCases")
  void remove_video_category(String who, Supplier<RequestPostProcessor> auth) throws Exception {
    var videoId    = UUID.randomUUID();
    var categoryId = UUID.randomUUID();

    willDoNothing().given(categoryService).removeCategoryFromVideo(videoId, categoryId);

    int s = mvc.perform(delete("/api/videos/{videoId}/categories/{categoryId}", videoId, categoryId)
            .with(auth.get())
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andReturn().getResponse().getStatus();

    assertNot401Or403(s);
  }

  @ParameterizedTest(name = "POST /api/live-streams/'{'liveStreamId'}'/categories - {0}")
  @MethodSource("authedCases")
  void add_livestream_categories(String who, Supplier<RequestPostProcessor> auth) throws Exception {
    var liveStreamId = UUID.randomUUID();
    var body         = om.writeValueAsString(List.of(UUID.randomUUID()));

    willDoNothing().given(categoryService).addCategoriesToLiveStream(eq(liveStreamId), anyList());

    int s = mvc.perform(post("/api/live-streams/{liveStreamId}/categories", liveStreamId)
            .with(auth.get())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andReturn().getResponse().getStatus();

    assertNot401Or403(s);
  }

  @ParameterizedTest(name = "PUT /api/live-streams/'{'liveStreamId'}'/categories - {0}")
  @MethodSource("authedCases")
  void replace_livestream_categories(String who, Supplier<RequestPostProcessor> auth) throws Exception {
    var liveStreamId = UUID.randomUUID();
    var body         = om.writeValueAsString(List.of(UUID.randomUUID()));

    willDoNothing().given(categoryService).replaceLiveStreamCategories(eq(liveStreamId), anyList());

    int s = mvc.perform(put("/api/live-streams/{liveStreamId}/categories", liveStreamId)
            .with(auth.get())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andReturn().getResponse().getStatus();

    assertNot401Or403(s);
  }

  @ParameterizedTest(name = "DELETE /api/live-streams/'{'liveStreamId'}'/categories/'{'categoryId'}' - {0}")
  @MethodSource("authedCases")
  void remove_livestream_category(String who, Supplier<RequestPostProcessor> auth) throws Exception {
    var liveStreamId = UUID.randomUUID();
    var categoryId   = UUID.randomUUID();

    willDoNothing().given(categoryService).removeCategoryFromLiveStream(liveStreamId, categoryId);

    int s = mvc.perform(delete("/api/live-streams/{liveStreamId}/categories/{categoryId}", liveStreamId, categoryId)
            .with(auth.get())
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andReturn().getResponse().getStatus();

    assertNot401Or403(s);
  }

  // ========================================================
  // ============= Ⅲ) 管理者専用（hasRole('ADMIN')） =========
  // ========================================================

  @ParameterizedTest(name = "POST /api/categories - {0}")
  @MethodSource("authedCases")
  void create_category_role_matrix(String who, Supplier<RequestPostProcessor> auth) throws Exception {
    var req = CategoryCreateRequestDTO.builder().name("Tech").build();
    var res = CategoryResponseDTO.builder().id(UUID.randomUUID()).name("Tech").build();

    given(categoryService.createCategory(any(CategoryCreateRequestDTO.class))).willReturn(res);

    int s = mvc.perform(post("/api/categories")
            .with(auth.get())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(req)))
        .andReturn().getResponse().getStatus();

    if ("ADMIN".equals(who)) assertNot401Or403(s);
    else org.junit.jupiter.api.Assertions.assertEquals(403, s);
  }

  @ParameterizedTest(name = "PUT /api/categories/'{'id'}' - {0}")
  @MethodSource("authedCases")
  void update_category_role_matrix(String who, Supplier<RequestPostProcessor> auth) throws Exception {
    UUID id  = UUID.randomUUID();
    var req = CategoryUpdateRequestDTO.builder().name("News").build();
    var res = CategoryResponseDTO.builder().id(id).name("News").build();

    given(categoryService.updateCategory(eq(id), any(CategoryUpdateRequestDTO.class))).willReturn(res);

    int s = mvc.perform(put("/api/categories/{id}", id)
            .with(auth.get())
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(req)))
        .andReturn().getResponse().getStatus();

    if ("ADMIN".equals(who)) assertNot401Or403(s);
    else org.junit.jupiter.api.Assertions.assertEquals(403, s);
  }

  @ParameterizedTest(name = "DELETE /api/categories/'{'id'}' - {0}")
  @MethodSource("authedCases")
  void delete_category_role_matrix(String who, Supplier<RequestPostProcessor> auth) throws Exception {
    UUID id = UUID.randomUUID();
    willDoNothing().given(categoryService).deleteCategory(id);

    int s = mvc.perform(delete("/api/categories/{id}", id)
            .with(auth.get())
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andReturn().getResponse().getStatus();

    if ("ADMIN".equals(who)) assertNot401Or403(s);
    else org.junit.jupiter.api.Assertions.assertEquals(403, s);
  }
}
