package com.example.docmgmt.controller;

import com.example.docmgmt.config.SecurityConfig;
import com.example.docmgmt.model.Document;
import com.example.docmgmt.service.QnAService;
import com.example.docmgmt.service.QnAServiceTest;
import com.example.docmgmt.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@WebMvcTest(QnAController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class QnAControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QnAService qnAService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser(roles = {"ADMIN", "EDITOR", "VIEWER"})
    public void testSearch_Success() throws Exception {
        Document doc = new Document();
        doc.setId(1L);
        doc.setTitle("Test Document");
        List<Document> documents = List.of(doc);

        when(qnAService.searchDocuments("test")).thenReturn(documents);

        mockMvc.perform(get("/api/qa/search").param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Test Document")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "EDITOR", "VIEWER"})
    public void testSearch_NoResults() throws Exception {
        when(qnAService.searchDocuments("empty")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/qa/search").param("query", "empty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
} 