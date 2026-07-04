package com.familyos.familyos.ruleengine.rules;

import static org.junit.jupiter.api.Assertions.*;

import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import com.familyos.familyos.ruleengine.dto.RuleDecision;
import com.familyos.familyos.ruleengine.dto.RuleResult;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SenderRuleTest {

    private SenderRule senderRule;

    @BeforeEach
    void setUp() {
        senderRule = new SenderRule();
    }

    @Test
    void testPriority() {
        assertEquals(10, senderRule.getPriority());
    }

    @Test
    void testMatchesSchoolSender() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc1",
            "Lincoln Elementary School",
            "Permission Slip",
            "Please sign the permission slip",
            List.of(),
            "high",
            "email"
        );

        Optional<RuleResult> result = senderRule.evaluate(doc);

        assertTrue(result.isPresent());
        assertEquals(RuleDecision.PROCESS, result.get().decision());
        assertEquals("SenderRule", result.get().matchedRule());
        assertTrue(result.get().reason().contains("school"));
        assertEquals(95, result.get().priorityScore());
    }

    @Test
    void testMatchesHospitalSender() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc2",
            "City Hospital Appointment",
            "Your Upcoming Appointment",
            "Your appointment is scheduled",
            List.of(),
            "high",
            "email"
        );

        Optional<RuleResult> result = senderRule.evaluate(doc);

        assertTrue(result.isPresent());
        assertEquals(RuleDecision.PROCESS, result.get().decision());
        assertTrue(result.get().reason().contains("hospital"));
    }

    @Test
    void testMatchesDoctorSender() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc3",
            "Dr. Smith Medical Office",
            "Prescription Ready",
            "Your prescription is ready",
            List.of(),
            "normal",
            "email"
        );

        Optional<RuleResult> result = senderRule.evaluate(doc);

        assertTrue(result.isPresent());
        assertEquals(RuleDecision.PROCESS, result.get().decision());
    }

    @Test
    void testMatchesBankSender() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc4",
            "First National Bank",
            "Account Statement",
            "Your monthly statement",
            List.of(),
            "normal",
            "email"
        );

        Optional<RuleResult> result = senderRule.evaluate(doc);

        assertTrue(result.isPresent());
        assertEquals(RuleDecision.PROCESS, result.get().decision());
    }

    @Test
    void testNoMatchForUnimportantSender() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc5",
            "Random Marketing Company",
            "Special Offer",
            "Limited time offer",
            List.of(),
            "low",
            "email"
        );

        Optional<RuleResult> result = senderRule.evaluate(doc);

        assertTrue(result.isEmpty());
    }

    @Test
    void testCaseInsensitiveMatching() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc6",
            "LINCOLN ELEMENTARY SCHOOL",
            "Permission Slip",
            "Please sign the permission slip",
            List.of(),
            "high",
            "email"
        );

        Optional<RuleResult> result = senderRule.evaluate(doc);

        assertTrue(result.isPresent());
    }

    @Test
    void testNullSender() {
        NormalizedDocument doc = new NormalizedDocument(
            "doc7",
            null,
            "Some Subject",
            "Some content",
            List.of(),
            "normal",
            "email"
        );

        Optional<RuleResult> result = senderRule.evaluate(doc);

        assertTrue(result.isEmpty());
    }

}
