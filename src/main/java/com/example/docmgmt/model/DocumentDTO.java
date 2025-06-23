package com.example.docmgmt.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class DocumentDTO {
    private final Long id;
    private final String title;
    private final String author;
    private final String type;

    public DocumentDTO(Long id, String title, String author, String type) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.type = type;
    }
}