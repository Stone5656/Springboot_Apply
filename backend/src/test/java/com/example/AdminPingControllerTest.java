// src/test/java/com/example/controller/AdminPingControllerTest.java
package com.example;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AdminPingControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("ADMIN は /api/admin/ping にアクセスできる (200)")
    @WithMockUser(username = "alice", roles = {"ADMIN"})
    void adminCanAccessPing() throws Exception {
        mvc.perform(get("/api/admin/ping"))
           .andExpect(status().isOk())
           .andExpect(content().string("pong-admin"));
    }

    @Test
    @DisplayName("USER は /api/admin/ping にアクセスできない (403)")
    @WithMockUser(username = "bob", roles = {"USER"})
    void userCannotAccessPing() throws Exception {
        mvc.perform(get("/api/admin/ping"))
           .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("MODERATOR も /api/admin/ping にアクセスできない (403) - 階層は下位包含のみ")
    @WithMockUser(username = "charlie", roles = {"MODERATOR"})
    void moderatorCannotAccessPing() throws Exception {
        mvc.perform(get("/api/admin/ping"))
           .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("未認証は /api/admin/ping で 401")
    void unauthenticatedGets401() throws Exception {
        mvc.perform(get("/api/admin/ping"))
           .andExpect(status().isUnauthorized());
    }
}
