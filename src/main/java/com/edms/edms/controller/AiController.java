package com.edms.edms.controller;

import com.edms.edms.service.DocumentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final DocumentService documentService;

    public AiController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/ask/{documentId}")
    public String askAI(@PathVariable Long documentId,
                        @RequestBody String question) {

        return documentService.askAI(documentId, question);
    }
}