package com.familyos.familyos.ruleengine.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import com.familyos.familyos.ruleengine.dto.RuleDecision;
import com.familyos.familyos.ruleengine.dto.RuleResult;
import com.familyos.familyos.ruleengine.model.Rule;
import com.familyos.familyos.ruleengine.rules.KeywordRule;
import com.familyos.familyos.ruleengine.rules.LabelRule;
import com.familyos.familyos.ruleengine.rules.SenderRule;
import java.util.List;
import org.junit.jupiter.api.Test;

class RuleEngineServiceTest {

    @Test
    void testEvaluateWithMatchingRule() {
        List<Rule> rules = List.of(new SenderRule());
        RuleEngineService engine = new RuleEngineService(rules);

        NormalizedDocument doc = new NormalizedDocument(
            "doc1",
            "Lincoln Elementary School",
            "Permission Slip",
            "Please sign",
            List.of(),
            "high",
            "email"
        );

        RuleResult result = engine.evaluate(doc);

        assertEquals(RuleDecision.PROCESS, result.decision());
        assertEquals("SenderRule", result.matchedRule());
    }

    @Test
    void testEvaluateWithPriorityOrder() {
        List<Rule> rules = List.of(
            new KeywordRule(),  // priority 30
            new LabelRule(),    // priority 20
            new SenderRule()    // priority 10
        );
        RuleEngineService engine = new RuleEngineService(rules);

        NormalizedDocument doc = new NormalizedDocument(
            "doc1",
            "Lincoln School",
            "Permission Slip",
            "This document requires your action",
            List.of(),
            "high",
            "email"
        );

        RuleResult result = engine.evaluate(doc);

        // Should match SenderRule first (lowest priority number = first)
        assertEquals("SenderRule", result.matchedRule());
    }

    @Test
    void testEvaluateWithNoMatchingRules() {
        List<Rule> rules = List.of(new SenderRule(), new LabelRule(), new KeywordRule());
        RuleEngineService engine = new RuleEngineService(rules);

        NormalizedDocument doc = new NormalizedDocument(
            "doc1",
            "Random Person",
            "Just saying hi",
            "How are you doing today?",
            List.of(),
            "normal",
            "email"
        );

        RuleResult result = engine.evaluate(doc);

        assertEquals(RuleDecision.LOW_PRIORITY, result.decision());
        assertEquals("DefaultRule", result.matchedRule());
        assertEquals(50, result.priorityScore());
    }

    @Test
    void testEvaluateReturnsFirstMatchingRule() {
        Rule mockRule1 = mock(Rule.class);
        when(mockRule1.getPriority()).thenReturn(10);
        when(mockRule1.evaluate(any())).thenReturn(java.util.Optional.empty());

        Rule mockRule2 = mock(Rule.class);
        when(mockRule2.getPriority()).thenReturn(20);
        RuleResult mockResult = new RuleResult(
            RuleDecision.PROCESS,
            "MockRule2",
            "Matched",
            85
        );
        when(mockRule2.evaluate(any())).thenReturn(java.util.Optional.of(mockResult));

        Rule mockRule3 = mock(Rule.class);
        when(mockRule3.getPriority()).thenReturn(30);

        List<Rule> rules = List.of(mockRule2, mockRule3, mockRule1);
        RuleEngineService engine = new RuleEngineService(rules);

        NormalizedDocument doc = new NormalizedDocument(
            "doc1", "sender", "subject", "content", List.of(), "normal", "email"
        );

        RuleResult result = engine.evaluate(doc);

        assertEquals("MockRule2", result.matchedRule());
        verify(mockRule2, times(1)).evaluate(any());
    }

    @Test
    void testEmptyRulesList() {
        List<Rule> rules = List.of();
        RuleEngineService engine = new RuleEngineService(rules);

        NormalizedDocument doc = new NormalizedDocument(
            "doc1", "sender", "subject", "content", List.of(), "normal", "email"
        );

        RuleResult result = engine.evaluate(doc);

        assertEquals(RuleDecision.LOW_PRIORITY, result.decision());
        assertEquals("DefaultRule", result.matchedRule());
    }

}
