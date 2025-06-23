package com.example.docmgmt.controller;

import com.example.docmgmt.model.DocumentDTO;
import com.example.docmgmt.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Operation(summary = "Upload a file with metadata")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadFileWithMetadata(
            @RequestPart("file") MultipartFile file,
            @RequestParam("metadata") String metadataJson) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try {
            byte[] fileBytes = file.getBytes();
            String originalFilename = file.getOriginalFilename();
            documentService.processDocumentAsync(fileBytes, originalFilename, metadataJson);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to read file content");
        }

        return ResponseEntity.accepted().body("Document upload accepted for processing with metadata");
    }

    @Operation(summary = "Get paginated documents with filters")
    @GetMapping
    public ResponseEntity<Page<DocumentDTO>> getDocuments(
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return ResponseEntity.ok(documentService.getDocuments(author, type, page, size, sort));
    }
}
