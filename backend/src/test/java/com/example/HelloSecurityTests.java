// src/test/java/com/example/HelloSecurityTests.java
package com.example;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // ← application-test.yml を読み込む
@TestMethodOrder(MethodOrderer.MethodName.class)
class HelloSecurityTests {

  private static final Logger log = LoggerFactory.getLogger(HelloSecurityTests.class);

  @Autowired
  MockMvc mvc;

  @Test
  void a_public_is_200() throws Exception {
    MvcResult res = mvc.perform(get("/api/hello/public"))
        .andDo(print())                // リクエスト/レスポンスを標準出力へ
        .andExpect(status().isOk())
        .andReturn();

    log.info("[public] status={}, body={}",
        res.getResponse().getStatus(), res.getResponse().getContentAsString());
  }

  @Test
  void b_private_is_401_when_anonymous() throws Exception {
    MvcResult res = mvc.perform(get("/api/hello/private"))
        .andDo(print())
        .andExpect(status().isUnauthorized()) // 未認証は 401
        .andReturn();

    log.info("[private anonymous] status={}, headers.WWW-Authenticate={}",
        res.getResponse().getStatus(), res.getResponse().getHeader("WWW-Authenticate"));
  }

  @Test
  void c_private_is_200_when_authenticated() throws Exception {
    MvcResult res = mvc.perform(
            get("/api/hello/private")
                // 認証済みユーザーを付与（JWT不要／Mockユーザー）
                .with(user("tester").authorities(new SimpleGrantedAuthority("USER"))))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

    log.info("[private auth] status={}, body={}",
        res.getResponse().getStatus(), res.getResponse().getContentAsString());
  }
}
