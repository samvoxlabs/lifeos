package com.familyos.familyos.domain.repository;

import com.familyos.familyos.domain.entity.Event;
import com.familyos.familyos.domain.entity.Extraction;
import com.familyos.familyos.domain.entity.ProcessingStatus;
import com.familyos.familyos.domain.entity.Reminder;
import com.familyos.familyos.domain.entity.SourceDocument;
import com.familyos.familyos.domain.entity.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIf("dockerAvailable")
class DomainRepositoryTest {

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

    @Autowired private SourceDocumentRepository sourceDocumentRepository;
    @Autowired private ExtractionRepository extractionRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private ReminderRepository reminderRepository;

    @Test
    void deduplicationLookupFindsExistingSourceDocument() {
        SourceDocument sourceDocument = new SourceDocument();
        sourceDocument.setProvider("google");
        sourceDocument.setExternalId("msg-123");
        sourceDocument.setSourceType("gmail");
        sourceDocument.setRawContent("raw");
        sourceDocument.setMetadata(Map.of("k", "v"));
        sourceDocument.setReceivedAt(LocalDateTime.now());
        sourceDocument.setProcessingStatus(ProcessingStatus.PROCESSED);
        sourceDocument = sourceDocumentRepository.save(sourceDocument);

        assertTrue(sourceDocumentRepository.findByProviderAndExternalIdAndSourceType("google", "msg-123", "gmail").isPresent());

        Extraction extraction = new Extraction();
        extraction.setSourceDocument(sourceDocument);
        extraction.setSummary("summary");
        extraction.setConfidence(0.90);
        extraction.setModel("gemini");
        extraction.setProvider("google");
        extraction.setPromptVersion("v1");
        extraction = extractionRepository.save(extraction);

        Task task = new Task();
        task.setTitle("Task title");
        task.setDescription("Task desc");
        task.setStatus("OPEN");
        task.setConfidence(0.9);
        task.setSourceDocument(sourceDocument);
        task.setExtraction(extraction);
        taskRepository.save(task);

        Event event = new Event();
        event.setTitle("Event title");
        event.setDescription("Event desc");
        event.setStatus("OPEN");
        event.setConfidence(0.8);
        event.setSourceDocument(sourceDocument);
        event.setExtraction(extraction);
        eventRepository.save(event);

        Reminder reminder = new Reminder();
        reminder.setTitle("Reminder title");
        reminder.setDescription("Reminder desc");
        reminder.setStatus("OPEN");
        reminder.setConfidence(0.7);
        reminder.setSourceDocument(sourceDocument);
        reminder.setExtraction(extraction);
        reminderRepository.save(reminder);

        assertEquals(1, taskRepository.findAll().size());
        assertEquals(1, eventRepository.findAll().size());
        assertEquals(1, reminderRepository.findAll().size());
        assertTrue(extractionRepository.findBySourceDocumentId(sourceDocument.getId()).isPresent());
    }
}
