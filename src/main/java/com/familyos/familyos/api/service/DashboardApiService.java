package com.familyos.familyos.api.service;

import com.familyos.familyos.api.dto.DashboardResponse;
import com.familyos.familyos.api.mapper.ApiDtoMapper;
import com.familyos.familyos.domain.repository.EventRepository;
import com.familyos.familyos.domain.repository.ReminderRepository;
import com.familyos.familyos.domain.repository.TaskRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardApiService {

    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;
    private final ReminderRepository reminderRepository;
    private final ApiDtoMapper apiDtoMapper;

    public DashboardApiService(
        TaskRepository taskRepository,
        EventRepository eventRepository,
        ReminderRepository reminderRepository,
        ApiDtoMapper apiDtoMapper
    ) {
        this.taskRepository = taskRepository;
        this.eventRepository = eventRepository;
        this.reminderRepository = reminderRepository;
        this.apiDtoMapper = apiDtoMapper;
    }

    public DashboardResponse getDashboard() {
        var recentTasksPage = taskRepository.findAll(
            PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        var upcomingEventsPage = eventRepository.findAll(
            PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "createdAt"))
        );
        var activeRemindersPage = reminderRepository.findAll(
            Specification.where((root, query, cb) -> cb.or(
                cb.equal(cb.upper(root.get("status")), "OPEN"),
                cb.equal(cb.upper(root.get("status")), "ACTIVE")
            )),
            PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        long pendingTasks = taskRepository.count(
            (root, query, cb) -> cb.or(
                cb.equal(cb.upper(root.get("status")), "PENDING"),
                cb.equal(cb.upper(root.get("status")), "OPEN")
            )
        );
        long upcomingEvents = eventRepository.count();
        long activeReminders = activeRemindersPage.getTotalElements();

        return new DashboardResponse(
            new DashboardResponse.Summary(pendingTasks, upcomingEvents, activeReminders),
            recentTasksPage.getContent().stream().map(apiDtoMapper::toTaskSummary).toList(),
            upcomingEventsPage.getContent().stream().map(apiDtoMapper::toEventResponse).toList(),
            activeRemindersPage.getContent().stream().map(apiDtoMapper::toReminderResponse).toList()
        );
    }
}
