package com.example.docmgmt.service;

import com.example.docmgmt.model.Document;
import com.example.docmgmt.model.DocumentDTO;
import com.example.docmgmt.repository.DocumentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private DocumentService documentService;

    @Test
    void testGetDocuments() {
        Document doc = new Document();
        doc.setId(1L);
        doc.setTitle("Test Doc");
        doc.setAuthor("John Doe");
        doc.setType("PDF");

        Page<Document> page = new PageImpl<>(List.of(doc));
        when(documentRepository.findByAuthorAndType(any(), any(), any(PageRequest.class))).thenReturn(page);

        Page<DocumentDTO> result = documentService.getDocuments("John Doe", "PDF", 0, 10, "createdAt,desc");

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Doc", result.getContent().get(0).getTitle());
        verify(documentRepository, times(1)).findByAuthorAndType(eq("John Doe"), eq("PDF"), any(PageRequest.class));
    }

    @Test
    void testProcessDocumentAsync() throws Exception {
        byte[] fileBytes = "hello world".getBytes();
        String filename = "test.txt";
        String metadataJson = "{\"author\":\"Test Author\",\"type\":\"TXT\"}";

        doNothing().when(rabbitTemplate).convertAndSend(eq("documentQueue"), any(Document.class));

        documentService.processDocumentAsync(fileBytes, filename, metadataJson);

        verify(rabbitTemplate, times(1)).convertAndSend(eq("documentQueue"), any(Document.class));
    }
} 