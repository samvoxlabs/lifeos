package com.familyos.familyos.ruleengine.rules;

import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import com.familyos.familyos.ruleengine.dto.RuleDecision;
import com.familyos.familyos.ruleengine.dto.RuleResult;
import com.familyos.familyos.ruleengine.model.Rule;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class KeywordRule implements Rule {

    private static final int PRIORITY = 30;
    
    private static final Set<String> PROCESS_KEYWORDS = new LinkedHashSet<>(Set.of(
        "appointment", "meeting", "deadline", "payment",
        "invoice", "permission", "vaccination", "interview",
        "application", "registration", "confirmation", "schedule",
        "reminder", "urgent", "important", "action",
        "approval", "request", "form", "sign", "submit",
        "due", "expir", "refund", "claim", "benefit"
    ));

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public Optional<RuleResult> evaluate(NormalizedDocument document) {
        String contentToSearch = buildSearchContent(document);

        for (String keyword : PROCESS_KEYWORDS) {
            if (contentToSearch.contains(keyword.toLowerCase())) {
                return Optional.of(
                    new RuleResult(
                        RuleDecision.PROCESS,
                        "KeywordRule",
                        "Matched keyword: " + keyword,
                        80
                    )
                );
            }
        }

        return Optional.empty();
    }

    private String buildSearchContent(NormalizedDocument document) {
        StringBuilder content = new StringBuilder();

        if (document.subject() != null) {
            content.append(document.subject()).append(" ");
        }
        if (document.content() != null) {
            content.append(document.content()).append(" ");
        }

        return content.toString().toLowerCase();
    }

}
