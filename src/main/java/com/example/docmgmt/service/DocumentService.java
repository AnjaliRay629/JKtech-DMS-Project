package com.example.docmgmt.service;
import com.example.docmgmt.model.Document;
import com.example.docmgmt.model.DocumentDTO;
import com.example.docmgmt.repository.DocumentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final Tika tika = new Tika();

    public DocumentService(DocumentRepository documentRepository, RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.documentRepository = documentRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Async
    public void processDocumentAsync(byte[] fileBytes, String filename, String metadataJson) {
        try {
            Map<String, String> metadata = objectMapper.readValue(metadataJson, Map.class);

            // Create temp file with correct extension
            File tempFile = File.createTempFile("upload-", "-" + filename);
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(fileBytes);
            }

            // Use AutoDetectParser for better parsing
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1); // unlimited length
            Metadata tikaMetadata = new Metadata();

            try (FileInputStream inputStream = new FileInputStream(tempFile)) {
                parser.parse(inputStream, handler, tikaMetadata, new ParseContext());
            }

            // Clean up
            tempFile.delete();

            String content = handler.toString();

            Document document = new Document();
            document.setTitle(filename);
            document.setContent(content);
            document.setAuthor(metadata.get("author"));
            document.setType(metadata.get("type"));
            document.setTsvector(content);
            document.setCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));

            rabbitTemplate.convertAndSend("documentQueue", document);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process document", e);
        }
    }

    public Page<DocumentDTO> getDocuments(String author, String type, int page, int size, String sort) {
        Sort sortObj = parseSort(sort);
        PageRequest pageRequest = PageRequest.of(page, size, sortObj);
        return documentRepository.findByAuthorAndType(author, type, pageRequest)
                .map(this::toDTO);
    }

    private Sort parseSort(String sort) {
        String[] parts = sort.split(",");
        return Sort.by(Sort.Direction.fromString(parts[1]), parts[0]);
    }

    private DocumentDTO toDTO(Document document) {
        return new DocumentDTO(document.getId(), document.getTitle(), document.getAuthor(), document.getType());
    }
}
