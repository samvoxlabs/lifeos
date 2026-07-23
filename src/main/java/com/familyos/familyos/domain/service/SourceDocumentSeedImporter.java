package com.familyos.familyos.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.familyos.familyos.domain.dto.ProcessExtractionRequest;
import com.familyos.familyos.domain.repository.SourceDocumentRepository;
import com.familyos.familyos.extraction.dto.ExtractionResult;
import com.familyos.familyos.ruleengine.dto.NormalizedDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
public class SourceDocumentSeedImporter implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SourceDocumentSeedImporter.class);

    private final SourceDocumentRepository sourceDocumentRepository;
    private final DomainPersistenceService domainPersistenceService;
    private final ResourcePatternResolver resourcePatternResolver;
    private final ObjectMapper objectMapper;

    public SourceDocumentSeedImporter(
        SourceDocumentRepository sourceDocumentRepository,
        DomainPersistenceService domainPersistenceService,
        ResourcePatternResolver resourcePatternResolver,
        ObjectMapper objectMapper
    ) {
        this.sourceDocumentRepository = sourceDocumentRepository;
        this.domainPersistenceService = domainPersistenceService;
        this.resourcePatternResolver = resourcePatternResolver;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        importSeedData(true);
    }

    public int importSeedData(boolean onlyWhenEmpty) throws Exception {
        if (onlyWhenEmpty && sourceDocumentRepository.count() > 0) {
            return 0;
        }

        Resource[] resources = resourcePatternResolver.getResources("classpath*:seed/*/*.json");
        if (resources.length == 0) {
            log.info("No seed data files found under classpath:seed/*/*.json");
            return 0;
        }

        int imported = 0;
        for (Resource resource : resources) {
            JsonNode root = objectMapper.readTree(resource.getInputStream());
            if (root.isArray()) {
                for (JsonNode node : root) {
                    imported += importNode(node, resource.getFilename());
                }
            } else {
                imported += importNode(root, resource.getFilename());
            }
        }
        log.info("Seed import complete. Imported {} source document(s)", imported);
        return imported;
    }

    private int importNode(JsonNode node, String fileName) {
        try {
            if (node.has("sourceDocument")) {
                NormalizedDocument sourceDocument = objectMapper.treeToValue(node.get("sourceDocument"), NormalizedDocument.class);
                JsonNode extractionResultNode = node.get("extractionResult");
                if (extractionResultNode != null && !extractionResultNode.isNull()) {
                    ExtractionResult extractionResult = objectMapper.treeToValue(extractionResultNode, ExtractionResult.class);
                    domainPersistenceService.process(new ProcessExtractionRequest(sourceDocument, extractionResult));
                } else {
                    domainPersistenceService.persistSourceDocument(sourceDocument);
                }
                return 1;
            }

            NormalizedDocument normalizedDocument = objectMapper.treeToValue(node, NormalizedDocument.class);
            domainPersistenceService.persistSourceDocument(normalizedDocument);
            return 1;
        } catch (Exception ex) {
            log.warn("Skipping invalid seed entry in {}: {}", fileName, ex.getMessage());
            return 0;
        }
    }
}
