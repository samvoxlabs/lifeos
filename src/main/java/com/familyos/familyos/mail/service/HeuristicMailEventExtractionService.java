package com.familyos.familyos.mail.service;

import com.familyos.familyos.mail.entity.MailMessage;
import com.familyos.familyos.mail.model.ExtractedMailEventCandidate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HeuristicMailEventExtractionService implements MailEventExtractionService {

    private static final List<String> ACTIONABLE_KEYWORDS = List.of(
            "pickup", "pick up", "dropoff", "drop off", "appointment", "practice", "meeting", "schedule"
    );
    private static final Pattern ISO_PATTERN =
            Pattern.compile("\\b\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(?::\\d{2})?(?:Z|[+-]\\d{2}:\\d{2})\\b");
    private static final Pattern DATE_TIME_PATTERN =
            Pattern.compile("\\b(\\d{4}-\\d{2}-\\d{2})\\s+(\\d{1,2}:\\d{2})(?:\\s*(AM|PM|am|pm))?\\b");
    private static final Pattern MONTH_PATTERN = Pattern.compile(
            "\\b([A-Za-z]{3,9}\\s+\\d{1,2},\\s*\\d{4})\\s*(?:at)?\\s*(\\d{1,2}:\\d{2}\\s*(?:AM|PM|am|pm))\\b"
    );
    private static final DateTimeFormatter MONTH_DATE = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("MMMM d, yyyy h:mm a")
            .toFormatter(Locale.ENGLISH);
    private static final DateTimeFormatter MONTH_DATE_SHORT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("MMM d, yyyy h:mm a")
            .toFormatter(Locale.ENGLISH);
    private static final DateTimeFormatter DATE_TIME_24H = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_TIME_12H = DateTimeFormatter.ofPattern("yyyy-MM-dd h:mm a", Locale.ENGLISH);

    @Override
    public Optional<ExtractedMailEventCandidate> extractFromMessage(MailMessage message) {
        String subject = blankToEmpty(message.getSubject());
        String snippet = blankToEmpty(message.getSnippet());
        String combined = subject + " " + snippet;
        String combinedLower = combined.toLowerCase(Locale.ROOT);
        if (ACTIONABLE_KEYWORDS.stream().noneMatch(combinedLower::contains)) {
            return Optional.empty();
        }

        OffsetDateTime start = parseDate(combined, message.getReceivedAt());
        if (start == null) {
            return Optional.empty();
        }

        String title = subject.isBlank() ? "Calendar event from email" : subject;
        return Optional.of(new ExtractedMailEventCandidate(title, start, start.plusHours(1), null, null, "other", "other", "medium", 0.7));
    }

    private OffsetDateTime parseDate(String text, OffsetDateTime fallback) {
        Matcher isoMatcher = ISO_PATTERN.matcher(text);
        if (isoMatcher.find()) {
            try {
                return OffsetDateTime.parse(isoMatcher.group());
            } catch (DateTimeParseException ignored) {
                // Continue fallbacks.
            }
        }

        Matcher dateTimeMatcher = DATE_TIME_PATTERN.matcher(text);
        if (dateTimeMatcher.find()) {
            String date = dateTimeMatcher.group(1);
            String time = dateTimeMatcher.group(2);
            String ampm = dateTimeMatcher.group(3);
            try {
                LocalDateTime local = ampm == null || ampm.isBlank()
                        ? LocalDateTime.parse(date + " " + time, DATE_TIME_24H)
                        : LocalDateTime.parse(date + " " + time + " " + ampm.toUpperCase(Locale.ROOT), DATE_TIME_12H);
                ZoneOffset offset = fallback == null ? ZoneOffset.UTC : fallback.getOffset();
                return local.atOffset(offset);
            } catch (DateTimeParseException ignored) {
                // Continue fallbacks.
            }
        }

        Matcher monthMatcher = MONTH_PATTERN.matcher(text);
        if (monthMatcher.find()) {
            String datePart = monthMatcher.group(1);
            String timePart = monthMatcher.group(2).toUpperCase(Locale.ROOT);
            try {
                LocalDateTime local = LocalDateTime.parse(datePart + " " + timePart, MONTH_DATE);
                ZoneOffset offset = fallback == null ? ZoneOffset.UTC : fallback.getOffset();
                return local.atOffset(offset);
            } catch (DateTimeParseException ignored) {
                try {
                    LocalDateTime local = LocalDateTime.parse(datePart + " " + timePart, MONTH_DATE_SHORT);
                    ZoneOffset offset = fallback == null ? ZoneOffset.UTC : fallback.getOffset();
                    return local.atOffset(offset);
                } catch (DateTimeParseException ignoredAgain) {
                    // Continue fallbacks.
                }
            }
        }

        return null;
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value;
    }
}
