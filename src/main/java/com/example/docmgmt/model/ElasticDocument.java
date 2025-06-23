package com.example.docmgmt.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Document(indexName = "documents")
public class ElasticDocument {

    @Id
    private String id = java.util.UUID.randomUUID().toString();

    private String title;
    private String content;
    private String author;
    private String type;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private Instant createdAt;

}
