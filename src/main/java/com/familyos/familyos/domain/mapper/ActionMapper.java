package com.familyos.familyos.domain.mapper;

import com.familyos.familyos.domain.entity.Action;
import com.familyos.familyos.domain.entity.Event;
import com.familyos.familyos.domain.entity.Extraction;
import com.familyos.familyos.domain.entity.Reminder;
import com.familyos.familyos.domain.entity.SourceDocument;
import com.familyos.familyos.domain.entity.Task;
import com.familyos.familyos.domain.exception.DomainValidationException;
import com.familyos.familyos.extraction.dto.ActionCandidate;
import com.familyos.familyos.extraction.dto.ExtractionResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ActionMapper {

    public List<Action> mapToEntities(ExtractionResult extractionResult, SourceDocument sourceDocument, Extraction extraction) {
        List<Action> mapped = new ArrayList<>();
        if (extractionResult.actions() == null) {
            return mapped;
        }

        for (ActionCandidate candidate : extractionResult.actions()) {
            if (candidate == null) {
                continue;
            }
            if (isBlank(candidate.type())) {
                throw new DomainValidationException("Action type is required");
            }
            if (isBlank(candidate.title())) {
                throw new DomainValidationException("Action title is required");
            }
            Action action = createAction(candidate.type());
            action.setTitle(candidate.title().trim());
            action.setDescription(candidate.description());
            action.setStatus("OPEN");
            action.setConfidence(extractionResult.confidence());
            action.setSourceDocument(sourceDocument);
            action.setExtraction(extraction);
            mapped.add(action);
        }
        return mapped;
    }

    private Action createAction(String type) {
        return switch (type.trim().toUpperCase(Locale.ROOT)) {
            case "TASK" -> new Task();
            case "EVENT" -> new Event();
            case "REMINDER" -> new Reminder();
            default -> throw new DomainValidationException("Unsupported action type: " + type);
        };
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
