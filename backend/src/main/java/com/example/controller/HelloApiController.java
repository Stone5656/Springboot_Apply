package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloApiController {

  @GetMapping("/api/hello")
  public String hello() {
    return "Hello Spring Boot API!";
  }
}
