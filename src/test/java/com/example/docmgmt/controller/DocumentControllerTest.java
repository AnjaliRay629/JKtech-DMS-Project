package com.example.docmgmt.controller;

import com.example.docmgmt.config.SecurityConfig;
import com.example.docmgmt.model.DocumentDTO;
import com.example.docmgmt.service.DocumentService;
import com.example.docmgmt.service.DocumentServiceTest;
import com.example.docmgmt.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser(roles = {"ADMIN", "EDITOR"})
    void testUploadFileWithMetadata_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
        String metadataJson = "{\"title\":\"Test Title\",\"author\":\"Test Author\"}";

        doNothing().when(documentService).processDocumentAsync(any(), any(), any());

        mockMvc.perform(multipart("/api/documents/upload")
                        .file(file)
                        .param("metadata", metadataJson))
                .andExpect(status().isAccepted());
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN", "EDITOR"})
    void testUploadFileWithMetadata_EmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, new byte[0]);
        String metadataJson = "{\"title\":\"Test Title\",\"author\":\"Test Author\"}";

        mockMvc.perform(multipart("/api/documents/upload")
                        .file(file)
                        .param("metadata", metadataJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "EDITOR"})
    void testGetDocuments() throws Exception {
        DocumentDTO dto = new DocumentDTO(1L, "Test Doc", "John", "PDF");
        Page<DocumentDTO> page = new PageImpl<>(List.of(dto));
        when(documentService.getDocuments(any(), any(), anyInt(), anyInt(), anyString())).thenReturn(page);

        mockMvc.perform(get("/api/documents?page=0&size=10&sort=createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Doc"));
    }
} 