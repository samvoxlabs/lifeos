package com.familyos.familyos.events.dto;

import java.util.List;

public record EventsListResponse(List<EnrichedEventResponse> events, int total, String nextCursor) {}
