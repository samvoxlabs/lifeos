package com.familyos.familyos.ruleengine.controller;

import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import com.familyos.familyos.ruleengine.dto.RuleResult;
import com.familyos.familyos.ruleengine.service.RuleEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rules")
public class RuleEngineController {

    private static final Logger logger = LoggerFactory.getLogger(RuleEngineController.class);
    
    private final RuleEngineService ruleEngineService;

    public RuleEngineController(RuleEngineService ruleEngineService) {
        this.ruleEngineService = ruleEngineService;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<RuleResult> evaluate(@RequestBody NormalizedDocument document) {
        logger.info("Received evaluation request for document: {}", document.id());
        try {
            RuleResult result = ruleEngineService.evaluate(document);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error evaluating document: {}", document.id(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
