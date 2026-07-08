package com.familyos.familyos.api.service;

import com.familyos.familyos.api.dto.EventResponse;
import com.familyos.familyos.api.dto.PagedResponse;
import com.familyos.familyos.api.exception.ApiNotFoundException;
import com.familyos.familyos.api.exception.ApiValidationException;
import com.familyos.familyos.api.mapper.ApiDtoMapper;
import com.familyos.familyos.domain.entity.Event;
import com.familyos.familyos.domain.repository.EventRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class EventApiService {

    private final EventRepository eventRepository;
    private final ApiDtoMapper apiDtoMapper;

    public EventApiService(EventRepository eventRepository, ApiDtoMapper apiDtoMapper) {
        this.eventRepository = eventRepository;
        this.apiDtoMapper = apiDtoMapper;
    }

    public PagedResponse<EventResponse> getEvents(LocalDateTime from, LocalDateTime to, Pageable pageable) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ApiValidationException("'from' must be before or equal to 'to'");
        }
        Page<EventResponse> page = eventRepository.findAll(eventSpecification(from, to), pageable)
            .map(apiDtoMapper::toEventResponse);
        return PagedResponse.from(page, pageable.getSort().toString());
    }

    public EventResponse getEvent(UUID id) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new ApiNotFoundException("Event not found: " + id));
        return apiDtoMapper.toEventResponse(event);
    }

    private Specification<Event> eventSpecification(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
