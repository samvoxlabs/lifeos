package com.familyos.familyos.api.service;

import com.familyos.familyos.api.dto.SearchResponse;
import com.familyos.familyos.api.mapper.ApiDtoMapper;
import com.familyos.familyos.domain.entity.Event;
import com.familyos.familyos.domain.entity.Reminder;
import com.familyos.familyos.domain.entity.Task;
import com.familyos.familyos.domain.repository.EventRepository;
import com.familyos.familyos.domain.repository.ReminderRepository;
import com.familyos.familyos.domain.repository.TaskRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchApiService {

    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;
    private final ReminderRepository reminderRepository;
    private final ApiDtoMapper apiDtoMapper;

    public SearchApiService(
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

    public SearchResponse search(String query) {
        String normalized = query == null ? "" : query.trim();
        if (normalized.isEmpty()) {
            return new SearchResponse(normalized, List.of(), List.of(), List.of());
        }

        Specification<Task> taskSpecification = textSearchSpec(normalized);
        Specification<Event> eventSpecification = textSearchSpec(normalized);
        Specification<Reminder> reminderSpecification = textSearchSpec(normalized);

        return new SearchResponse(
            normalized,
            taskRepository.findAll(taskSpecification).stream().map(apiDtoMapper::toTaskResponse).toList(),
            eventRepository.findAll(eventSpecification).stream().map(apiDtoMapper::toEventResponse).toList(),
            reminderRepository.findAll(reminderSpecification).stream().map(apiDtoMapper::toReminderResponse).toList()
        );
    }

    private <T> Specification<T> textSearchSpec(String query) {
        return (root, cq, cb) -> {
            String pattern = "%" + query.toLowerCase() + "%";
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get("title"), "")), pattern));
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get("description"), "")), pattern));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
