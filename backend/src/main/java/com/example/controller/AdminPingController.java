// src/main/java/com/example/controller/AdminPingController.java
package com.example.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminPingController {

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ping")
    public String ping() {
        return "pong-admin";
    }
}
