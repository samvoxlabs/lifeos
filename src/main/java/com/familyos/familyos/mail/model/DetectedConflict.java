package com.familyos.familyos.mail.model;

import java.time.OffsetDateTime;
import java.util.List;

public record DetectedConflict(
        String conflictingEventId,
        String conflictingEventSource,
        OffsetDateTime overlapStart,
        OffsetDateTime overlapEnd,
        List<ResolutionOption> suggestedResolutions
) {
}
