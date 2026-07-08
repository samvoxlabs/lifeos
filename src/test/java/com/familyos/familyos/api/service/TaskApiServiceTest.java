package com.familyos.familyos.api.service;

import com.familyos.familyos.api.exception.ApiValidationException;
import com.familyos.familyos.api.mapper.ApiDtoMapper;
import com.familyos.familyos.domain.entity.Task;
import com.familyos.familyos.domain.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskApiServiceTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskApiService taskApiService;

    @BeforeEach
    void setUp() {
        taskApiService = new TaskApiService(taskRepository, new ApiDtoMapper());
    }

    @Test
    void supportsPaginationAndFilters() {
        Task task = new Task();
        task.setTitle("Confirm attendance");
        task.setStatus("OPEN");
        task.setConfidence(0.9);

        when(taskRepository.findAll(any(Specification.class), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of(task), PageRequest.of(0, 10), 1));

        var response = taskApiService.getTasks("OPEN", "HIGH", PageRequest.of(0, 10));

        assertEquals(1, response.content().size());
        assertEquals("HIGH", response.content().get(0).priority());
    }

    @Test
    void rejectsInvalidPriorityFilter() {
        assertThrows(ApiValidationException.class, () ->
            taskApiService.getTasks(null, "CRITICAL", PageRequest.of(0, 10))
        );
    }
}
