// src/main/java/com/example/controller/HelloController.java
package com.example.controller;

import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hello")
public class HelloController {

  // 公開
  @GetMapping("/public")
  public ResponseEntity<String> publicHello() {
    return ResponseEntity.ok("hello, world (public)");
  }

  // 認証必須（Securityのルールでブロック、通過後はPrincipalあり）
  @GetMapping("/private")
  public ResponseEntity<String> privateHello(Principal principal) {
    return ResponseEntity.ok("hello, " + principal.getName());
  }
}
