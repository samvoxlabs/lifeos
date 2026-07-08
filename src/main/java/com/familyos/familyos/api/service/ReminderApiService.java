package com.familyos.familyos.api.service;

import com.familyos.familyos.api.dto.PagedResponse;
import com.familyos.familyos.api.dto.ReminderResponse;
import com.familyos.familyos.api.exception.ApiNotFoundException;
import com.familyos.familyos.api.exception.ApiValidationException;
import com.familyos.familyos.api.mapper.ApiDtoMapper;
import com.familyos.familyos.domain.entity.Reminder;
import com.familyos.familyos.domain.repository.ReminderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
public class ReminderApiService {

    private final ReminderRepository reminderRepository;
    private final ApiDtoMapper apiDtoMapper;

    public ReminderApiService(ReminderRepository reminderRepository, ApiDtoMapper apiDtoMapper) {
        this.reminderRepository = reminderRepository;
        this.apiDtoMapper = apiDtoMapper;
    }

    public PagedResponse<ReminderResponse> getReminders(String status, Pageable pageable) {
        validateStatus(status);
        Specification<Reminder> spec = (root, query, cb) -> {
            if (status == null || status.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(cb.upper(root.get("status")), status.trim().toUpperCase(Locale.ROOT));
        };
        Page<ReminderResponse> page = reminderRepository.findAll(spec, pageable).map(apiDtoMapper::toReminderResponse);
        return PagedResponse.from(page, pageable.getSort().toString());
    }

    public ReminderResponse getReminder(UUID id) {
        Reminder reminder = reminderRepository.findById(id)
            .orElseThrow(() -> new ApiNotFoundException("Reminder not found: " + id));
        return apiDtoMapper.toReminderResponse(reminder);
    }

    private void validateStatus(String status) {
        if (status == null || status.isBlank()) {
            return;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("ACTIVE") && !normalized.equals("COMPLETED") && !normalized.equals("DISMISSED")
            && !normalized.equals("OPEN")) {
            throw new ApiValidationException("Invalid reminder status filter: " + status);
        }
    }
}
