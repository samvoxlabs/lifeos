package com.familyos.familyos.ruleengine.rules;

import static org.junit.jupiter.api.Assertions.*;

import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import com.familyos.familyos.ruleengine.dto.RuleDecision;
import com.familyos.familyos.ruleengine.dto.RuleResult;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KeywordRuleTest {

    private KeywordRule keywordRule;

    @BeforeEach
    void setUp() {
        keywordRule = new KeywordRule();
    }

    @Test
    void testPriority() {
        assertEquals(30, keywordRule.getPriority());
    }

    @Test
    void testMatchesAppointmentKeyword() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc1",
            "Doctor",
            "Your appointment is scheduled",
            "Please confirm your appointment for tomorrow",
            List.of(),
            "high",
            "email"
        );

        Optional<RuleResult> result = keywordRule.evaluate(doc);

        assertTrue(result.isPresent());
        assertEquals(RuleDecision.PROCESS, result.get().decision());
        assertTrue(result.get().reason().startsWith("Matched keyword: "));
        assertEquals(80, result.get().priorityScore());
    }

    @Test
    void testMatchesMeetingKeyword() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc2",
            "Manager",
            "Team meeting rescheduled",
            "The meeting has been moved to Friday",
            List.of(),
            "normal",
            "email"
        );

        Optional<RuleResult> result = keywordRule.evaluate(doc);

        assertTrue(result.isPresent());
        assertEquals(RuleDecision.PROCESS, result.get().decision());
    }

    @Test
    void testMatchesDeadlineKeyword() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc3",
            "Project Manager",
            "Deadline approaching",
            "The deadline for project submission is next Friday",
            List.of(),
            "high",
            "email"
        );

        Optional<RuleResult> result = keywordRule.evaluate(doc);

        assertTrue(result.isPresent());
        assertEquals(RuleDecision.PROCESS, result.get().decision());
    }

    @Test
    void testMatchesInvoiceKeyword() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc4",
            "Vendor",
            "Invoice #12345",
            "Please find attached invoice for payment",
            List.of(),
            "normal",
            "email"
        );

        Optional<RuleResult> result = keywordRule.evaluate(doc);

        assertTrue(result.isPresent());
        assertEquals(RuleDecision.PROCESS, result.get().decision());
    }

    @Test
    void testMatchesVaccinationKeyword() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc5",
            "Health Department",
            "Vaccination reminder",
            "Your child needs their vaccination shots",
            List.of(),
            "high",
            "email"
        );

        Optional<RuleResult> result = keywordRule.evaluate(doc);

        assertTrue(result.isPresent());
        assertEquals(RuleDecision.PROCESS, result.get().decision());
    }

    @Test
    void testMatchesPermissionSlipKeyword() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc6",
            "School",
            "Permission slip required",
            "Please sign and return the permission slip",
            List.of(),
            "high",
            "email"
        );

        Optional<RuleResult> result = keywordRule.evaluate(doc);

        assertTrue(result.isPresent());
        assertEquals(RuleDecision.PROCESS, result.get().decision());
    }

    @Test
    void testNoMatchForNonKeywordContent() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc7",
            "Friend",
            "Just checking in",
            "How have you been lately? Let's catch up soon",
            List.of(),
            "normal",
            "email"
        );

        Optional<RuleResult> result = keywordRule.evaluate(doc);

        assertTrue(result.isEmpty());
    }

    @Test
    void testCaseInsensitiveMatching() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc8",
            "Sender",
            "APPOINTMENT REMINDER",
            "Your APPOINTMENT is confirmed",
            List.of(),
            "normal",
            "email"
        );

        Optional<RuleResult> result = keywordRule.evaluate(doc);

        assertTrue(result.isPresent());
        assertEquals(RuleDecision.PROCESS, result.get().decision());
    }

    @Test
    void testKeywordInContent() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc9",
            "Sender",
            "Some Subject",
            "This is important because there is a payment due tomorrow",
            List.of(),
            "normal",
            "email"
        );

        Optional<RuleResult> result = keywordRule.evaluate(doc);

        assertTrue(result.isPresent());
        assertEquals(RuleDecision.PROCESS, result.get().decision());
    }

    @Test
    void testNullSubjectAndContent() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc10",
            "Sender",
            null,
            null,
            List.of(),
            "normal",
            "email"
        );

        Optional<RuleResult> result = keywordRule.evaluate(doc);

        assertTrue(result.isEmpty());
    }

}
