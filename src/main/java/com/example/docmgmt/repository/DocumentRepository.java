package com.example.docmgmt.repository;

import com.example.docmgmt.model.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query(value = "SELECT * FROM documents WHERE to_tsvector('english', content) @@ plainto_tsquery(?1)", nativeQuery = true)
    List<Document> findByFullTextSearch(String query);

    @Query("SELECT d FROM Document d WHERE (:author IS NULL OR d.author = :author) AND (:type IS NULL OR d.type = :type)")
    Page<Document> findByAuthorAndType(String author, String type, Pageable pageable);
}