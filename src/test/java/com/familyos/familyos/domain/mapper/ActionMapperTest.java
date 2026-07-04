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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActionMapperTest {

    private ActionMapper actionMapper;

    @BeforeEach
    void setUp() {
        actionMapper = new ActionMapper();
    }

    @Test
    void mapsActionCandidatesToDomainSubtypes() {
        ExtractionResult extractionResult = new ExtractionResult(
            "Summary",
            0.95,
            List.of(
                new ActionCandidate("TASK", "Buy milk", "Today", null),
                new ActionCandidate("EVENT", "School meeting", "Parent meeting", "2026-07-05T18:00:00"),
                new ActionCandidate("REMINDER", "Call mom", "Evening", null)
            )
        );

        SourceDocument sourceDocument = new SourceDocument();
        Extraction extraction = new Extraction();

        List<Action> actions = actionMapper.mapToEntities(extractionResult, sourceDocument, extraction);

        assertEquals(3, actions.size());
        assertInstanceOf(Task.class, actions.get(0));
        assertInstanceOf(Event.class, actions.get(1));
        assertInstanceOf(Reminder.class, actions.get(2));
        assertEquals("OPEN", actions.get(0).getStatus());
        assertEquals(0.95, actions.get(0).getConfidence());
    }

    @Test
    void throwsForUnsupportedActionType() {
        ExtractionResult extractionResult = new ExtractionResult(
            "Summary",
            0.80,
            List.of(new ActionCandidate("NOTE", "Something", "desc", null))
        );

        assertThrows(
            DomainValidationException.class,
            () -> actionMapper.mapToEntities(extractionResult, new SourceDocument(), new Extraction())
        );
    }

    @Test
    void throwsWhenTitleMissing() {
        ExtractionResult extractionResult = new ExtractionResult(
            "Summary",
            0.80,
            List.of(new ActionCandidate("TASK", "   ", "desc", null))
        );

        assertThrows(
            DomainValidationException.class,
            () -> actionMapper.mapToEntities(extractionResult, new SourceDocument(), new Extraction())
        );
    }
}
