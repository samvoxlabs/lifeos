package com.familyos.familyos.api.service;

import com.familyos.familyos.api.mapper.ApiDtoMapper;
import com.familyos.familyos.domain.entity.Event;
import com.familyos.familyos.domain.entity.Reminder;
import com.familyos.familyos.domain.entity.Task;
import com.familyos.familyos.domain.repository.EventRepository;
import com.familyos.familyos.domain.repository.ReminderRepository;
import com.familyos.familyos.domain.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardApiServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private EventRepository eventRepository;
    @Mock private ReminderRepository reminderRepository;

    private DashboardApiService dashboardApiService;

    @BeforeEach
    void setUp() {
        dashboardApiService = new DashboardApiService(taskRepository, eventRepository, reminderRepository, new ApiDtoMapper());
    }

    @Test
    void returnsDashboardSummary() {
        Task task = new Task();
        task.setTitle("Task 1");
        task.setStatus("OPEN");

        Event event = new Event();
        event.setTitle("Event 1");
        event.setStatus("OPEN");

        Reminder reminder = new Reminder();
        reminder.setTitle("Reminder 1");
        reminder.setStatus("OPEN");

        when(taskRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(task)));
        when(eventRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(event)));
        when(reminderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(reminder)));
        when(taskRepository.count(any(Specification.class))).thenReturn(3L);
        when(eventRepository.count()).thenReturn(2L);

        var response = dashboardApiService.getDashboard();

        assertEquals(3L, response.summary().pendingTasks());
        assertEquals(2L, response.summary().upcomingEvents());
        assertEquals(1L, response.summary().activeReminders());
        assertEquals(1, response.recentTasks().size());
    }
}
