package com.familyos.familyos.ruleengine.rules;

import static org.junit.jupiter.api.Assertions.*;

import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import com.familyos.familyos.ruleengine.dto.RuleDecision;
import com.familyos.familyos.ruleengine.dto.RuleResult;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LabelRuleTest {

    private LabelRule labelRule;

    @BeforeEach
    void setUp() {
        labelRule = new LabelRule();
    }

    @Test
    void testPriority() {
        assertEquals(20, labelRule.getPriority());
    }

    @Test
    void testIgnoresPromotionsLabel() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc1",
            "Some Store",
            "Special Offer",
            "Check out our latest deals",
            List.of("promotions"),
            "low",
            "email"
        );

        Optional<RuleResult> result = labelRule.evaluate(doc);

        assertTrue(result.isPresent());
        assertEquals(RuleDecision.IGNORE, result.get().decision());
        assertEquals("LabelRule", result.get().matchedRule());
        assertTrue(result.get().reason().contains("promotions"));
    }

    @Test
    void testIgnoresSocialLabel() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc2",
            "Friend",
            "Check this out",
            "Hey, found something cool",
            List.of("social"),
            "normal",
            "email"
        );

        Optional<RuleResult> result = labelRule.evaluate(doc);

        assertTrue(result.isPresent());
        assertEquals(RuleDecision.IGNORE, result.get().decision());
    }

    @Test
    void testIgnoresSpamLabel() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc3",
            "Unknown Sender",
            "You won a prize!",
            "Claim your prize now",
            List.of("spam"),
            "low",
            "email"
        );

        Optional<RuleResult> result = labelRule.evaluate(doc);

        assertTrue(result.isPresent());
        assertEquals(RuleDecision.IGNORE, result.get().decision());
    }

    @Test
    void testNoMatchForImportantLabel() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc4",
            "School",
            "Permission Slip",
            "Please sign",
            List.of("important"),
            "high",
            "email"
        );

        Optional<RuleResult> result = labelRule.evaluate(doc);

        assertTrue(result.isEmpty());
    }

    @Test
    void testEmptyLabels() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc5",
            "Some Sender",
            "Some Subject",
            "Some content",
            List.of(),
            "normal",
            "email"
        );

        Optional<RuleResult> result = labelRule.evaluate(doc);

        assertTrue(result.isEmpty());
    }

    @Test
    void testNullLabels() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc6",
            "Some Sender",
            "Some Subject",
            "Some content",
            null,
            "normal",
            "email"
        );

        Optional<RuleResult> result = labelRule.evaluate(doc);

        assertTrue(result.isEmpty());
    }

    @Test
    void testMultipleLabels_FirstIgnored() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc7",
            "Sender",
            "Subject",
            "Content",
            List.of("promotions", "marketing", "important"),
            "normal",
            "email"
        );

        Optional<RuleResult> result = labelRule.evaluate(doc);

        assertTrue(result.isPresent());
        assertEquals(RuleDecision.IGNORE, result.get().decision());
    }

    @Test
    void testCaseInsensitiveMatching() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc8",
            "Sender",
            "Subject",
            "Content",
            List.of("PROMOTIONS"),
            "normal",
            "email"
        );

        Optional<RuleResult> result = labelRule.evaluate(doc);

        assertTrue(result.isPresent());
        assertEquals(RuleDecision.IGNORE, result.get().decision());
    }

}
