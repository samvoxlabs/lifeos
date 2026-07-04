package com.familyos.familyos.ruleengine.rules;

import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import com.familyos.familyos.ruleengine.dto.RuleDecision;
import com.familyos.familyos.ruleengine.dto.RuleResult;
import com.familyos.familyos.ruleengine.model.Rule;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SenderRule implements Rule {

    private static final int PRIORITY = 10;
    
    private static final Set<String> IMPORTANT_SENDERS = Set.of(
        "school", "hospital", "doctor", "dr.", "clinic", "dentist", "pharmacy", "insurance",
        "bank", "credit union", "irs", "tax", "government", "social security"
    );

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public Optional<RuleResult> evaluate(NormalizedDocument document) {
        if (document.sender() == null) {
            return Optional.empty();
        }

        String senderLower = document.sender().toLowerCase();

        for (String importantSender : IMPORTANT_SENDERS) {
            if (senderLower.contains(importantSender)) {
                return Optional.of(
                    new RuleResult(
                        RuleDecision.PROCESS,
                        "SenderRule",
                        "Matched important sender: " + importantSender,
                        95
                    )
                );
            }
        }

        return Optional.empty();
    }

}
