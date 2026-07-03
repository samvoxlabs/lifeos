package com.familyos.familyos.repository;

import com.familyos.familyos.authentication.entity.OAuthToken;
import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.repository.OAuthAccountRepository;
import com.familyos.familyos.authentication.repository.OAuthTokenRepository;
import com.familyos.familyos.authentication.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIf("dockerAvailable")
class AuthenticationRepositoryTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("familyos_test")
            .withUsername("familyos")
            .withPassword("familyos");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> {
            ensureContainerStarted();
            return POSTGRES.getJdbcUrl();
        });
        registry.add("spring.datasource.username", () -> {
            ensureContainerStarted();
            return POSTGRES.getUsername();
        });
        registry.add("spring.datasource.password", () -> {
            ensureContainerStarted();
            return POSTGRES.getPassword();
        });
        registry.add("spring.datasource.driver-class-name", () -> {
            ensureContainerStarted();
            return POSTGRES.getDriverClassName();
        });
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
    }

    static boolean dockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable ex) {
            return false;
        }
    }

    static void ensureContainerStarted() {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuthAccountRepository oauthAccountRepository;

    @Autowired
    private OAuthTokenRepository oauthTokenRepository;

    @Test
    void userRepositoryFindsByEmail() {
        User saved = userRepository.save(new User("user@example.com", "Test User", "google"));

        assertTrue(userRepository.findByEmail("user@example.com").isPresent());
        assertEquals(saved.getId(), userRepository.findByEmail("user@example.com").orElseThrow().getId());
    }

    @Test
    void oauthAccountAndTokenRepositoriesPersistNormalizedData() {
        User user = userRepository.save(new User("user2@example.com", "Test User", "google"));
        var account = oauthAccountRepository.save(new com.familyos.familyos.authentication.entity.OAuthAccount(
                user,
                "google",
                "subject-1",
                "user2@example.com",
                "Test User"
        ));
        OAuthToken token = oauthTokenRepository.save(new OAuthToken(
                account,
                "access-token",
                "refresh-token",
                "Bearer",
                String.join(" ", Set.of("openid", "email")),
                LocalDateTime.now().plusHours(1)
        ));

        assertTrue(oauthAccountRepository.findByUserAndProvider(user, "google").isPresent());
        assertEquals(account.getId(), oauthAccountRepository.findByUserAndProvider(user, "google").orElseThrow().getId());
        assertTrue(oauthTokenRepository.findByAccount(account).isPresent());
        assertEquals(token.getId(), oauthTokenRepository.findByAccount(account).orElseThrow().getId());

        oauthTokenRepository.deleteByAccount(account);
        assertTrue(oauthTokenRepository.findByAccount(account).isEmpty());
    }
}
