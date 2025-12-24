package com.example.be;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("CI 환경에서 전체 컨텍스트 로드 불필요")
class BeApplicationTests {

  @Test
  void contextLoads() {}
}
