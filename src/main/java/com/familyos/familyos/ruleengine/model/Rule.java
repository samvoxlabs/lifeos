package com.familyos.familyos.ruleengine.model;

import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import com.familyos.familyos.ruleengine.dto.RuleResult;
import java.util.Optional;

public interface Rule {

    int getPriority();

    Optional<RuleResult> evaluate(NormalizedDocument document);

}
