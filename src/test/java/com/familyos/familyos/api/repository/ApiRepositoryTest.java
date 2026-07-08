package com.familyos.familyos.api.repository;

import com.familyos.familyos.domain.entity.Event;
import com.familyos.familyos.domain.entity.Extraction;
import com.familyos.familyos.domain.entity.ProcessingStatus;
import com.familyos.familyos.domain.entity.Reminder;
import com.familyos.familyos.domain.entity.SourceDocument;
import com.familyos.familyos.domain.entity.Task;
import com.familyos.familyos.domain.repository.EventRepository;
import com.familyos.familyos.domain.repository.ExtractionRepository;
import com.familyos.familyos.domain.repository.ReminderRepository;
import com.familyos.familyos.domain.repository.SourceDocumentRepository;
import com.familyos.familyos.domain.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIf("dockerAvailable")
class ApiRepositoryTest {

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
        registry.add("spring.datasource.username", () -> POSTGRES.getUsername());
        registry.add("spring.datasource.password", () -> POSTGRES.getPassword());
        registry.add("spring.datasource.driver-class-name", () -> POSTGRES.getDriverClassName());
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
    void supportsSearchAndPaginationQueries() {
        SourceDocument sourceDocument = new SourceDocument();
        sourceDocument.setProvider("google");
        sourceDocument.setExternalId("repo-test-msg");
        sourceDocument.setSourceType("gmail");
        sourceDocument.setRawContent("raw");
        sourceDocument.setMetadata(Map.of("k", "v"));
        sourceDocument.setReceivedAt(LocalDateTime.now());
        sourceDocument.setProcessingStatus(ProcessingStatus.PROCESSED);
        sourceDocument = sourceDocumentRepository.save(sourceDocument);

        Extraction extraction = new Extraction();
        extraction.setSourceDocument(sourceDocument);
        extraction.setSummary("summary");
        extraction.setConfidence(0.9);
        extraction.setModel("model");
        extraction.setProvider("provider");
        extraction.setPromptVersion("v1");
        extraction = extractionRepository.save(extraction);

        Task task = new Task();
        task.setTitle("Pay school fees");
        task.setDescription("Complete payment");
        task.setStatus("OPEN");
        task.setConfidence(0.92);
        task.setSourceDocument(sourceDocument);
        task.setExtraction(extraction);
        taskRepository.save(task);

        Event event = new Event();
        event.setTitle("Doctor appointment");
        event.setDescription("Clinic visit");
        event.setStatus("OPEN");
        event.setConfidence(0.8);
        event.setSourceDocument(sourceDocument);
        event.setExtraction(extraction);
        eventRepository.save(event);

        Reminder reminder = new Reminder();
        reminder.setTitle("Pay electricity bill");
        reminder.setDescription("Due tomorrow");
        reminder.setStatus("ACTIVE");
        reminder.setConfidence(0.7);
        reminder.setSourceDocument(sourceDocument);
        reminder.setExtraction(extraction);
        reminderRepository.save(reminder);

        var taskPage = taskRepository.findAll(PageRequest.of(0, 10, Sort.by("createdAt").descending()));
        var eventPage = eventRepository.findAll((root, query, cb) ->
            cb.like(cb.lower(root.get("title")), "%appointment%"), PageRequest.of(0, 10));
        var reminderPage = reminderRepository.findAll((root, query, cb) ->
            cb.equal(cb.upper(root.get("status")), "ACTIVE"), PageRequest.of(0, 10));

        assertEquals(1, taskPage.getTotalElements());
        assertEquals(1, eventPage.getTotalElements());
        assertEquals(1, reminderPage.getTotalElements());
    }
}
