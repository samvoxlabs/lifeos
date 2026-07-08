package com.familyos.familyos.api.mapper;

import com.familyos.familyos.api.dto.EventResponse;
import com.familyos.familyos.api.dto.ReminderResponse;
import com.familyos.familyos.api.dto.SourceDocumentResponse;
import com.familyos.familyos.api.dto.TaskResponse;
import com.familyos.familyos.api.dto.TaskSummary;
import com.familyos.familyos.api.dto.TimelineItem;
import com.familyos.familyos.domain.entity.Event;
import com.familyos.familyos.domain.entity.Reminder;
import com.familyos.familyos.domain.entity.SourceDocument;
import com.familyos.familyos.domain.entity.Task;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ApiDtoMapper {

    public TaskResponse toTaskResponse(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            toPriority(task.getConfidence()),
            task.getConfidence(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }

    public TaskSummary toTaskSummary(Task task) {
        return new TaskSummary(
            task.getId(),
            task.getTitle(),
            task.getStatus(),
            task.getCreatedAt()
        );
    }

    public EventResponse toEventResponse(Event event) {
        return new EventResponse(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getStatus(),
            event.getConfidence(),
            event.getCreatedAt(),
            event.getUpdatedAt()
        );
    }

    public ReminderResponse toReminderResponse(Reminder reminder) {
        return new ReminderResponse(
            reminder.getId(),
            reminder.getTitle(),
            reminder.getDescription(),
            reminder.getStatus(),
            reminder.getConfidence(),
            reminder.getCreatedAt(),
            reminder.getUpdatedAt()
        );
    }

    public SourceDocumentResponse toSourceDocumentResponse(SourceDocument sourceDocument) {
        return new SourceDocumentResponse(
            sourceDocument.getId(),
            sourceDocument.getProvider(),
            sourceDocument.getSourceType(),
            sourceDocument.getExternalId(),
            sourceDocument.getSubject(),
            sourceDocument.getRawContent(),
            sourceDocument.getMetadata() == null ? Map.of() : sourceDocument.getMetadata(),
            sourceDocument.getProcessingStatus().name(),
            sourceDocument.getReceivedAt(),
            sourceDocument.getCreatedAt(),
            sourceDocument.getUpdatedAt()
        );
    }

    public TimelineItem toTimelineTaskItem(Task task) {
        return new TimelineItem(
            task.getId(),
            "TASK",
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getCreatedAt()
        );
    }

    public TimelineItem toTimelineEventItem(Event event) {
        return new TimelineItem(
            event.getId(),
            "EVENT",
            event.getTitle(),
            event.getDescription(),
            event.getStatus(),
            event.getCreatedAt()
        );
    }

    public TimelineItem toTimelineReminderItem(Reminder reminder) {
        return new TimelineItem(
            reminder.getId(),
            "REMINDER",
            reminder.getTitle(),
            reminder.getDescription(),
            reminder.getStatus(),
            reminder.getCreatedAt()
        );
    }

    public String toPriority(Double confidence) {
        if (confidence == null) {
            return "UNKNOWN";
        }
        if (confidence >= 0.8d) {
            return "HIGH";
        }
        if (confidence >= 0.5d) {
            return "MEDIUM";
        }
        return "LOW";
    }
}
