package com.familyos.familyos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

@SpringBootTest
class FamilyosApplicationTests {

	@MockBean
	private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

	@Test
	void contextLoads() {
	}

}
