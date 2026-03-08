package com.edms.edms.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import com.edms.edms.entity.Document;
import com.edms.edms.entity.DocumentVersion;
import com.edms.edms.entity.Folder;
import com.edms.edms.entity.User;
import com.edms.edms.repository.UserRepository;
import com.edms.edms.service.DocumentPermissionService;
import com.edms.edms.service.DocumentService;
import jakarta.servlet.http.HttpSession;
import com.edms.edms.repository.DocumentPermissionRepository;
import com.edms.edms.entity.DocumentPermission;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import com.edms.edms.service.AiService;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentPermissionService permissionService;
    private final UserRepository userRepository;
    private final DocumentPermissionRepository permissionRepository;
    private final AiService aiService;


    public DocumentController(DocumentService documentService,
                              DocumentPermissionService permissionService,
                              UserRepository userRepository,
                              DocumentPermissionRepository permissionRepository,
                              AiService aiService) {
        this.documentService = documentService;
        this.permissionService = permissionService;
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.aiService = aiService;
    }

    // ===============================
    // UPLOAD DOCUMENT (v1)
    // ===============================
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public Document uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folderId") Long folderId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "tags", required = false) String tags) throws IOException {

        if (file.isEmpty()) {
            throw new RuntimeException("Uploaded file is empty");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            throw new RuntimeException("No authentication found");
        }

        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return documentService.saveDocument(
                file,
                title != null ? title : file.getOriginalFilename(),
                tags,
                folderId,
                user
        );
    }


    // ===============================
    // UPLOAD NEW VERSION
    // ===============================
    @PostMapping(value = "/{documentId}/upload-version", consumes = "multipart/form-data")
    public DocumentVersion uploadNewVersion(
            @PathVariable Long documentId,
            @RequestParam("file") MultipartFile file) throws IOException {

        // Validate file empty
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Call service method (service will handle validation + saving)
        return documentService.uploadNewVersion(documentId, file);
    }
    // ===============================
    // PREVIEW CURRENT VERSION
    // ===============================
    @GetMapping(value = "/{documentId}/preview", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> previewDocument(
            @PathVariable Long documentId) throws Exception {

        return documentService.previewDocument(documentId);
    }

    // ===============================
    // DOWNLOAD CURRENT VERSION
    // ===============================
    @GetMapping(value = "/{documentId}/download",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadCurrent(
            @PathVariable Long documentId) throws Exception {

        return documentService.downloadCurrentVersion(documentId);
    }

    // ===============================
    // DOWNLOAD SPECIFIC VERSION
    // ===============================
    @GetMapping("/versions/{versionId}/download")
    public ResponseEntity<Resource> downloadByVersion(
            @PathVariable Long versionId) throws Exception {

        return documentService.downloadByVersionId(versionId);
    }

    // ===============================
    // LIST ALL DOCUMENTS
    // ===============================
    @GetMapping
    public List<Document> getAllDocuments() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return documentService.getAllDocumentsForUser(user);
    }

    // ===============================
    // LIST DOCUMENTS BY FOLDER
    // ===============================
    @GetMapping("/folders/{folderId}/documents")
    public List<Document> getDocumentsByFolder(
            @PathVariable Long folderId) {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return documentService.getDocumentsByFolder(folderId, user);
    }

    // ===============================
    // LIST VERSION HISTORY
    // ===============================
    @GetMapping("/{documentId}/versions")
    public List<DocumentVersion> getDocumentVersions(
            @PathVariable Long documentId) {

        return documentService.getVersionsByDocumentId(documentId);
    }

    // ===============================
    // SEARCH DOCUMENTS BY TITLE
    // ===============================
    @GetMapping("/search")
    public List<Document> searchDocuments(@RequestParam String title) {

        return documentService.searchDocuments(title);
    }

    @GetMapping("/{id}/permissions")
    @ResponseBody
    public List<DocumentPermission> getPermissions(@PathVariable Long id) {
        return permissionRepository.findByDocumentId(id);
    }

    // ===============================
// ASK AI ABOUT DOCUMENT
// ===============================
    @PostMapping("/{documentId}/ask")
    public ResponseEntity<?> askQuestion(
            @PathVariable Long documentId,
            @RequestBody Map<String, String> body) {

        String question = body.get("question");

        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Question cannot be empty");
        }

        String response =
                aiService.askQuestion(documentId, question);

        return ResponseEntity.ok(response);
    }

}
