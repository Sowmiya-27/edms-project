package com.edms.edms.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.edms.edms.entity.Document;
import com.edms.edms.entity.DocumentVersion;
import com.edms.edms.entity.Folder;
import com.edms.edms.entity.User;
import com.edms.edms.repository.UserRepository;
import com.edms.edms.service.DocumentPermissionService;
import com.edms.edms.service.DocumentService;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentPermissionService permissionService;
    private final UserRepository userRepository;

    public DocumentController(DocumentService documentService,
                              DocumentPermissionService permissionService,
                              UserRepository userRepository) {
        this.documentService = documentService;
        this.permissionService = permissionService;
        this.userRepository = userRepository;
    }

    // ===============================
    // UPLOAD DOCUMENT (v1)
    // ===============================
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public Document uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folderId") Long folderId) throws IOException {

        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String uploadDir = "D:/edms_uploads/";
        new File(uploadDir).mkdirs();

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String filePath = uploadDir + fileName;

        file.transferTo(new File(filePath));

        Folder folder = documentService.getFolderById(folderId);

        Document document = new Document();
        document.setFileName(fileName);
        document.setTitle(file.getOriginalFilename());
        document.setFileType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setFilePath(filePath);
        document.setFolder(folder);

        return documentService.saveDocument(document);
    }

    // ===============================
    // UPLOAD NEW VERSION
    // ===============================
    @PostMapping(value = "/{documentId}/upload-version", consumes = "multipart/form-data")
    public DocumentVersion uploadNewVersion(
            @PathVariable Long documentId,
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String uploadDir = "D:/edms_uploads/";
        new File(uploadDir).mkdirs();

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String filePath = uploadDir + fileName;

        file.transferTo(new File(filePath));

        return documentService.uploadNewVersion(documentId, filePath);
    }

    // ===============================
    // PREVIEW CURRENT VERSION
    // ===============================
    @GetMapping(value = "/{documentId}/preview", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> previewDocument(@PathVariable Long documentId) throws Exception {
        return documentService.previewDocument(documentId);
    }

    // ===============================
    // DOWNLOAD CURRENT VERSION
    // ===============================
    @GetMapping(value = "/{documentId}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadCurrent(@PathVariable Long documentId) throws Exception {
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
        return documentService.getAllDocuments();
    }

    // ===============================
    // LIST DOCUMENTS BY FOLDER
    // ===============================
    @GetMapping("/folders/{folderId}/documents")
    public List<Document> getDocumentsByFolder(
            @PathVariable Long folderId) {

        return documentService.getDocumentsByFolder(folderId);
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
    public List<Document> search(@RequestParam String title) {
        return documentService.searchByTitle(title);
    }
}
