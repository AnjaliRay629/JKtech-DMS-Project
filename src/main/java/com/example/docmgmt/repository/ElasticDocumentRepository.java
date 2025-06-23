package com.example.docmgmt.repository;

import com.example.docmgmt.model.ElasticDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElasticDocumentRepository extends ElasticsearchRepository<ElasticDocument, String> {
    List<ElasticDocument> findByContentContainingIgnoreCase(String keyword);
}
