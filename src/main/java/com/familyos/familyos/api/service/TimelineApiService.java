package com.familyos.familyos.api.service;

import com.familyos.familyos.api.dto.TimelineItem;
import com.familyos.familyos.api.mapper.ApiDtoMapper;
import com.familyos.familyos.domain.repository.EventRepository;
import com.familyos.familyos.domain.repository.ReminderRepository;
import com.familyos.familyos.domain.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class TimelineApiService {

    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;
    private final ReminderRepository reminderRepository;
    private final ApiDtoMapper apiDtoMapper;

    public TimelineApiService(
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

    public List<TimelineItem> getTimeline() {
        List<TimelineItem> taskItems = taskRepository.findAll().stream().map(apiDtoMapper::toTimelineTaskItem).toList();
        List<TimelineItem> eventItems = eventRepository.findAll().stream().map(apiDtoMapper::toTimelineEventItem).toList();
        List<TimelineItem> reminderItems = reminderRepository.findAll().stream().map(apiDtoMapper::toTimelineReminderItem).toList();

        return Stream.of(taskItems, eventItems, reminderItems)
            .flatMap(List::stream)
            .sorted(Comparator.comparing(TimelineItem::date, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
            .toList();
    }
}
