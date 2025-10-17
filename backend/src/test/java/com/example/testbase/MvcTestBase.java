// src/test/java/com/example/testbase/MvcTestBase.java
package com.example.testbase;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class MvcTestBase {
  @Autowired protected MockMvc mvc;
  @Autowired protected ObjectMapper om;
}
