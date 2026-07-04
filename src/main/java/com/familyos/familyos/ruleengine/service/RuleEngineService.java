package com.familyos.familyos.ruleengine.service;

import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import com.familyos.familyos.ruleengine.dto.RuleDecision;
import com.familyos.familyos.ruleengine.dto.RuleResult;
import com.familyos.familyos.ruleengine.model.Rule;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RuleEngineService {

    private static final Logger logger = LoggerFactory.getLogger(RuleEngineService.class);
    
    private final List<Rule> rules;

    public RuleEngineService(List<Rule> rules) {
        this.rules = rules.stream()
            .sorted(Comparator.comparingInt(Rule::getPriority))
            .toList();
        logger.info("RuleEngine initialized with {} rules in priority order", this.rules.size());
    }

    public RuleResult evaluate(NormalizedDocument document) {
        logger.debug("Evaluating document: {}", document.id());

        for (Rule rule : rules) {
            Optional<RuleResult> result = rule.evaluate(document);
            if (result.isPresent()) {
                logger.info(
                    "Document {} matched rule {} with decision: {}",
                    document.id(),
                    result.get().matchedRule(),
                    result.get().decision()
                );
                return result.get();
            }
        }

        return getDefaultDecision(document);
    }

    private RuleResult getDefaultDecision(NormalizedDocument document) {
        logger.debug("Document {} did not match any rules, applying default decision", document.id());
        return new RuleResult(
            RuleDecision.LOW_PRIORITY,
            "DefaultRule",
            "No rules matched, defaulting to LOW_PRIORITY",
            50
        );
    }

}
