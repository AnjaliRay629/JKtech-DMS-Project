package com.example.docmgmt.service;

import com.example.docmgmt.model.Document;
import com.example.docmgmt.model.ElasticDocument;
import com.example.docmgmt.repository.ElasticDocumentRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QnAService {

    private final ElasticDocumentRepository elasticRepository;

    public QnAService(ElasticDocumentRepository elasticRepository) {
        this.elasticRepository = elasticRepository;
    }

    @Cacheable(value = "qaResults", key = "#query")
    public List<Document> searchDocuments(String query) {
        List<ElasticDocument> elasticDocs = elasticRepository.findByContentContainingIgnoreCase(query);
        return elasticDocs.stream().map(this::toDocument).collect(Collectors.toList());
    }

    private Document toDocument(ElasticDocument edoc) {
        Document doc = new Document();
        doc.setTitle(edoc.getTitle());
        doc.setAuthor(edoc.getAuthor());
        doc.setContent(edoc.getContent());
        doc.setType(edoc.getType());
        doc.setTsvector(edoc.getContent()); // optional
        return doc;
    }
}
