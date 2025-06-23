package com.example.docmgmt.messaginglistner;

import com.example.docmgmt.model.Document;
import com.example.docmgmt.model.ElasticDocument;
import com.example.docmgmt.repository.DocumentRepository;
import com.example.docmgmt.repository.ElasticDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class DocumentQueueListener {

    private static final Logger logger = LoggerFactory.getLogger(DocumentQueueListener.class);

    private final DocumentRepository documentRepository;
    private final ElasticDocumentRepository elasticRepository;

    public DocumentQueueListener(DocumentRepository documentRepository, ElasticDocumentRepository elasticRepository) {
        this.documentRepository = documentRepository;
        this.elasticRepository = elasticRepository;
    }

    @RabbitListener(queues = "documentQueue")
    public void processDocument(Document document) {
        try {
            if (document == null) {
                logger.error("Received null document from documentQueue");
                return;
            }

            logger.info("Processing document: {}", document.getTitle());

            // Save to DB
            Document savedDoc = documentRepository.save(document);
            logger.info("Document saved to DB: {}", savedDoc.getTitle());

            // Index in Elasticsearch
            ElasticDocument elasticDoc = new ElasticDocument();
            elasticDoc.setTitle(savedDoc.getTitle());
            elasticDoc.setAuthor(savedDoc.getAuthor());
            elasticDoc.setContent(savedDoc.getContent());
            elasticDoc.setType(savedDoc.getType());
            elasticDoc.setCreatedAt(Instant.now());
            elasticRepository.save(elasticDoc);
            logger.info("Document indexed in Elasticsearch: {}", elasticDoc.getTitle());

        } catch (Exception e) {
            logger.error("Error processing document {}: {}", document.getTitle(), e.getMessage(), e);
            throw new RuntimeException("Failed to process document", e);
        }
    }
}
