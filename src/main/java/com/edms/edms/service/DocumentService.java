package com.edms.edms.service;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.edms.edms.entity.Document;
import com.edms.edms.entity.DocumentVersion;
import com.edms.edms.entity.Folder;
import com.edms.edms.entity.User;
import com.edms.edms.repository.DocumentRepository;
import com.edms.edms.repository.DocumentVersionRepository;
import com.edms.edms.repository.FolderRepository;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FolderRepository folderRepository;
    private final DocumentVersionRepository documentVersionRepository;

    public DocumentService(DocumentRepository documentRepository,
                           FolderRepository folderRepository,
                           DocumentVersionRepository documentVersionRepository) {
        this.documentRepository = documentRepository;
        this.folderRepository = folderRepository;
        this.documentVersionRepository = documentVersionRepository;
    }

    // ===============================
    // SAVE DOCUMENT (v1)
    // ===============================
    public Document saveDocument(Document document) {

        Document savedDocument = documentRepository.save(document);

        DocumentVersion version = new DocumentVersion();
        version.setDocument(savedDocument);
        version.setVersionNumber(1);
        version.setFilePath(savedDocument.getFilePath());
        version.setUploadedAt(LocalDateTime.now());
        version.setCurrent(true);

        documentVersionRepository.save(version);

        return savedDocument;
    }

    // ===============================
    // UPLOAD NEW VERSION
    // ===============================
    public DocumentVersion uploadNewVersion(Long documentId, String filePath) {

        Document document = getDocumentById(documentId);

        List<DocumentVersion> versions =
                documentVersionRepository.findByDocumentId(documentId);

        for (DocumentVersion v : versions) {
            v.setCurrent(false);
        }
        documentVersionRepository.saveAll(versions);

        DocumentVersion newVersion = new DocumentVersion();
        newVersion.setDocument(document);
        newVersion.setVersionNumber(versions.size() + 1);
        newVersion.setFilePath(filePath);
        newVersion.setUploadedAt(LocalDateTime.now());
        newVersion.setCurrent(true);

        return documentVersionRepository.save(newVersion);
    }

    // ===============================
    // GET DOCUMENT BY ID
    // ===============================
    public Document getDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new RuntimeException("Document not found with id: " + documentId));
    }

    // ===============================
    // GET DOCUMENTS BY FOLDER
    // ===============================
    public List<Document> getDocumentsByFolder(Long folderId) {
        return documentRepository.findByFolderId(folderId);
    }

    // ===============================
    // GET FOLDER BY ID
    // ===============================
    public Folder getFolderById(Long folderId) {
        return folderRepository.findById(folderId)
                .orElseThrow(() ->
                        new RuntimeException("Folder not found with id: " + folderId));
    }

    // ===============================
// GET ALL DOCUMENTS
// ===============================
    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }


    // ===============================
    // SEARCH DOCUMENTS
    // ===============================
    public List<Document> searchByTitle(String title) {
        return documentRepository.findByTitleContainingIgnoreCase(title);
    }

    // ===============================
    // PREVIEW CURRENT VERSION
    // ===============================
    public ResponseEntity<byte[]> previewDocument(Long documentId) throws Exception {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        DocumentVersion currentVersion = document.getVersions().stream()
                .filter(DocumentVersion::isCurrent)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No version found"));

        File file = new File(currentVersion.getFilePath());
        byte[] content = Files.readAllBytes(file.toPath());

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        if (document.getFileType() != null) {
            mediaType = MediaType.parseMediaType(document.getFileType());
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getTitle() + "\"")
                .body(content);
    }



    // ===============================
    // DOWNLOAD CURRENT VERSION
    // ===============================
    public ResponseEntity<byte[]> downloadCurrentVersion(Long documentId) throws Exception {

        DocumentVersion current = documentVersionRepository
                .findByDocumentId(documentId)
                .stream()
                .filter(DocumentVersion::isCurrent)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Current version not found"));

        File file = new File(current.getFilePath());

        if (!file.exists()) {
            throw new RuntimeException("File not found: " + file.getPath());
        }

        byte[] data = Files.readAllBytes(file.toPath());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getName() + "\"")
                .contentLength(data.length)
                .body(data);
    }
    // ===============================
// LIST VERSION HISTORY
// ===============================

    public List<DocumentVersion> getVersionsByDocumentId(Long documentId) {
        return documentVersionRepository.findByDocumentId(documentId);
    }

    // ===============================
    // DOWNLOAD SPECIFIC VERSION
    // ===============================
    public ResponseEntity<Resource> downloadByVersionId(Long versionId) throws Exception {

        DocumentVersion version = documentVersionRepository.findById(versionId)
                .orElseThrow(() ->
                        new RuntimeException("Version not found"));

        File file = new File(version.getFilePath());

        if (!file.exists()) {
            throw new RuntimeException("File not found on disk");
        }

        ByteArrayResource resource =
                new ByteArrayResource(Files.readAllBytes(file.toPath()));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getName() + "\"")
                .contentLength(file.length())
                .body(resource);
    }
}
