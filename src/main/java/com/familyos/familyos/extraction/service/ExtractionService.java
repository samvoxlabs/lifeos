package com.familyos.familyos.extraction.service;

import com.familyos.familyos.extraction.dto.ExtractionResponse;
import com.familyos.familyos.extraction.dto.ExtractionResult;
import com.familyos.familyos.extraction.parser.ExtractionParser;
import com.familyos.familyos.extraction.prompt.PromptBuilder;
import com.familyos.familyos.llm.LlmRequest;
import com.familyos.familyos.llm.LlmResponse;
import com.familyos.familyos.service.LlmService;
import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import com.familyos.familyos.ruleengine.dto.RuleDecision;
import com.familyos.familyos.ruleengine.service.RuleEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExtractionService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExtractionService.class);
    
    private final RuleEngineService ruleEngineService;
    private final PromptBuilder promptBuilder;
    private final LlmService llmService;
    private final ExtractionParser parser;
    
    public ExtractionService(
            RuleEngineService ruleEngineService,
            PromptBuilder promptBuilder,
            LlmService llmService,
            ExtractionParser parser) {
        this.ruleEngineService = ruleEngineService;
        this.promptBuilder = promptBuilder;
        this.llmService = llmService;
        this.parser = parser;
    }
    
    public ExtractionResponse process(NormalizedDocument document) {
        logger.info("Processing document: {}", document.id());
        
        // Step 1: Apply Rule Engine
        var ruleResult = ruleEngineService.evaluate(document);
        logger.debug("Rule Engine result for {}: {}", document.id(), ruleResult.decision());
        
        // Step 2: If not PROCESS, skip LLM invocation
        if (ruleResult.decision() != RuleDecision.PROCESS) {
            logger.info("Document {} skipped by Rule Engine: {}", document.id(), ruleResult.decision());
            return ExtractionResponse.skipped(ruleResult);
        }
        
        try {
            // Step 3: Build prompt
            LlmRequest llmRequest = promptBuilder.buildEmailExtractionRequest(document);
            logger.debug("Prompt built for document {}", document.id());
            
            // Step 4: Invoke LLM
            LlmResponse llmResponse = llmService.generate(
                null,
                llmRequest.useCase(),
                llmRequest.systemPrompt(),
                llmRequest.userContent(),
                llmRequest.metadata()
            );
            logger.debug("LLM response received for document {}", document.id());
            
            // Step 5: Parse response
            ExtractionResult result = parser.parse(llmResponse.content());
            logger.info("Extraction successful for document {}: confidence={}", 
                document.id(), result.confidence());
            
            return ExtractionResponse.success(result);
            
        } catch (Exception e) {
            logger.error("Extraction failed for document {}: {}", document.id(), e.getMessage(), e);
            return ExtractionResponse.error("Extraction failed: " + e.getMessage());
        }
    }
}
