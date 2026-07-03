package com.familyos.familyos;

import com.familyos.familyos.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

@SpringBootTest
@Import(TestSecurityConfig.class)
class FamilyosApplicationTests {

	@MockBean
	private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

	@Test
	void contextLoads() {
	}

}
