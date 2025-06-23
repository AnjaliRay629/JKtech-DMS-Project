package com.example.docmgmt.controller;

import com.example.docmgmt.model.Document;
import com.example.docmgmt.service.QnAService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/qa")
@SecurityRequirement(name = "bearerAuth")
public class QnAController {

    private final QnAService qnAService;

    public QnAController(QnAService qnAService) {
        this.qnAService = qnAService;
    }

    @Operation(summary = "Search documents by keyword")
    @GetMapping("/search")
    public ResponseEntity<List<Document>> search(@RequestParam String query) {
        return ResponseEntity.ok(qnAService.searchDocuments(query));
    }
}