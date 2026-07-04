package com.familyos.familyos.usecases.emailextraction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EmailExtractionResponse(
  List<ExtractedEmail> emails
) {

  public EmailExtractionResponse {
    emails = emails == null ? List.of() : List.copyOf(emails);
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ExtractedEmail(
    String subject,
    String from,
    String summary,
    String category,
    String priority,
    List<ActionItem> actionItems,
    List<EventItem> events,
    List<String> people,
    List<String> followUps
  ) {

    public ExtractedEmail {
      actionItems = actionItems == null ? List.of() : List.copyOf(actionItems);
      events = events == null ? List.of() : List.copyOf(events);
      people = people == null ? List.of() : List.copyOf(people);
      followUps = followUps == null ? List.of() : List.copyOf(followUps);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ActionItem(
    String title,
    String dueDate
  ) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record EventItem(
    String title,
    String date
  ) {}
}
