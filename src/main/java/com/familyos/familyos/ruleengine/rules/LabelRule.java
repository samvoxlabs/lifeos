package com.familyos.familyos.ruleengine.rules;

import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import com.familyos.familyos.ruleengine.dto.RuleDecision;
import com.familyos.familyos.ruleengine.dto.RuleResult;
import com.familyos.familyos.ruleengine.model.Rule;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class LabelRule implements Rule {

    private static final int PRIORITY = 20;
    
    private static final Set<String> IGNORE_LABELS = Set.of(
        "promotions", "social", "spam", "newsletters",
        "marketing", "ads", "commercial", "notifications"
    );

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public Optional<RuleResult> evaluate(NormalizedDocument document) {
        if (document.labels() == null || document.labels().isEmpty()) {
            return Optional.empty();
        }

        for (String label : document.labels()) {
            String labelLower = label.toLowerCase();
            for (String ignoreLabel : IGNORE_LABELS) {
                if (labelLower.contains(ignoreLabel)) {
                    return Optional.of(
                        new RuleResult(
                            RuleDecision.IGNORE,
                            "LabelRule",
                            "Matched ignore label: " + ignoreLabel,
                            10
                        )
                    );
                }
            }
        }

        return Optional.empty();
    }

}
