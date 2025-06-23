package com.example.docmgmt.service;

import com.example.docmgmt.model.Document;
import com.example.docmgmt.model.ElasticDocument;
import com.example.docmgmt.repository.ElasticDocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QnAServiceTest {

    @Mock
    private ElasticDocumentRepository elasticRepository;

    @InjectMocks
    private QnAService qnAService;

    @Test
   public void testSearchDocuments() {
        ElasticDocument edoc = new ElasticDocument();
        edoc.setTitle("Test Doc");
        edoc.setAuthor("John Doe");
        edoc.setContent("This is a test document.");
        edoc.setType("PDF");

        when(elasticRepository.findByContentContainingIgnoreCase("test")).thenReturn(List.of(edoc));

        List<Document> result = qnAService.searchDocuments("test");

        assertEquals(1, result.size());
        assertEquals("Test Doc", result.get(0).getTitle());
        verify(elasticRepository, times(1)).findByContentContainingIgnoreCase("test");
    }
} 