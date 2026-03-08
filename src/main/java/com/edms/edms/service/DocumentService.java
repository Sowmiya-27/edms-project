package com.edms.edms.service;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

import com.edms.edms.dto.DocumentTypeStatsDTO;
import com.edms.edms.repository.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.edms.edms.entity.Document;
import com.edms.edms.entity.DocumentPermission;
import com.edms.edms.entity.DocumentVersion;
import com.edms.edms.entity.Folder;
import com.edms.edms.entity.PermissionType;
import com.edms.edms.entity.User;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.springframework.beans.factory.annotation.Value;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FolderRepository folderRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentPermissionRepository permissionRepository;
    private final AiService aiService;
    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public DocumentService(DocumentRepository documentRepository,
                           FolderRepository folderRepository,
                           DocumentVersionRepository documentVersionRepository,
                           DocumentPermissionRepository permissionRepository,
                           AiService aiService,
                           UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.folderRepository = folderRepository;
        this.documentVersionRepository = documentVersionRepository;
        this.permissionRepository = permissionRepository;
        this.aiService = aiService;
        this.userRepository = userRepository;
    }

    // ===============================
    // SAVE DOCUMENT (v1) + DUPLICATE CHECK
    // ===============================

    public DocumentVersion uploadNewVersion(Long documentId, MultipartFile file) throws IOException {

        // 1️⃣ Validate file
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is required");
        }

        // Validate file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("File size exceeds 10MB limit");
        }

        // Validate file type
        String contentType = file.getContentType();

        if (!contentType.equals("application/pdf") &&
                !contentType.equals("text/plain") &&
                !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            throw new RuntimeException("Only PDF, DOCX, and TXT files are allowed");
        }

        // 2️⃣ Find document
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // 3️⃣ Save file to folder
        String uploadDir = "uploads/";

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String filePath = uploadDir + fileName;

        File destFile = new File(filePath);
        file.transferTo(destFile);

        // 4️⃣ Mark old versions as NOT current
        List<DocumentVersion> existingVersions =
                documentVersionRepository.findByDocumentId(documentId);

        for (DocumentVersion v : existingVersions) {
            if (v.isCurrent()) {
                v.setCurrent(false);
                documentVersionRepository.save(v);
            }
        }

        // 5️⃣ Calculate next version number
        int nextVersionNumber = existingVersions.stream()
                .mapToInt(DocumentVersion::getVersionNumber)
                .max()
                .orElse(0) + 1;

        // 6️⃣ Create new version
        DocumentVersion newVersion = new DocumentVersion();
        newVersion.setDocument(document);
        newVersion.setFilePath(filePath);
        newVersion.setVersionNumber(nextVersionNumber);
        newVersion.setCurrent(true);
        newVersion.setUploadedAt(LocalDateTime.now());

        // 7️⃣ Save version
        return documentVersionRepository.save(newVersion);
    }

    public Document saveDocument(MultipartFile file,
                                 String title,
                                 String tags,
                                 Long folderId,
                                 User user) throws IOException {

        // 1️⃣ Validate file
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Uploaded file is empty");
        }

        // 2️⃣ Check folder
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        // 3️⃣ Create upload directory
        new File(uploadDir).mkdirs();

        // 4️⃣ Save file to disk
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String filePath = uploadDir + fileName;

        file.transferTo(new File(filePath));

        // 5️⃣ Create document
        Document document = new Document();
        document.setTitle(title);
        document.setTags(tags);
        document.setFileName(fileName);
        document.setFilePath(filePath);
        document.setFileType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setOwner(user);
        document.setFolder(folder);

        Document savedDoc = documentRepository.save(document);

        // 6️⃣ Send document to AI indexing
        aiService.indexDocument(savedDoc.getId(), new File(filePath));

        // 7️⃣ Create first version (v1)
        DocumentVersion version = new DocumentVersion();
        version.setDocument(savedDoc);
        version.setFilePath(filePath);
        version.setVersionNumber(1);
        version.setCurrent(true);
        version.setUploadedAt(LocalDateTime.now());

        documentVersionRepository.save(version);

        return savedDoc;
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
    public List<Document> getDocumentsByFolder(Long folderId, User user) {
        return documentRepository.findByFolderIdAndOwner(folderId, user);
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
    public List<Document> getAllDocumentsForUser(User user) {

        // 1️⃣ Documents owned by user
        List<Document> ownedDocs = documentRepository.findByOwner(user);

        // 2️⃣ Documents shared with user
        List<DocumentPermission> sharedPermissions =
                permissionRepository.findByUser(user);

        List<Document> sharedDocs = sharedPermissions.stream()
                .map(DocumentPermission::getDocument)
                .toList();

        // 3️⃣ Public documents
        List<Document> publicDocs =
                documentRepository.findByIsPublicTrue();

        // Combine all
        ownedDocs.addAll(sharedDocs);

        for (Document doc : publicDocs) {
            if (!ownedDocs.contains(doc)) {
                ownedDocs.add(doc);
            }
        }

        return ownedDocs;
    }

    // ===============================
    // SEARCH DOCUMENTS BY TITLE
    // ===============================
    public List<Document> searchByTitle(String title) {
        return documentRepository.findByTitleContainingIgnoreCase(title);
    }

    // ===============================
    // PREVIEW CURRENT VERSION
    // ===============================
    public ResponseEntity<byte[]> previewDocument(Long documentId) throws Exception {

        Document document = getDocumentById(documentId);

        DocumentVersion currentVersion = documentVersionRepository
                .findByDocumentId(documentId)
                .stream()
                .filter(DocumentVersion::isCurrent)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No current version found"));

        File file = new File(currentVersion.getFilePath());
        byte[] content = Files.readAllBytes(file.toPath());

        MediaType mediaType;

        if (file.getName().endsWith(".pdf")) {
            mediaType = MediaType.APPLICATION_PDF;
        } else if (file.getName().endsWith(".txt")) {
            mediaType = MediaType.TEXT_PLAIN;
        } else {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getName() + "\"")
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

    // ===============================
// SHARE DOCUMENT
// ===============================
    public void shareDocument(Document document, User user,
                              PermissionType permissionType) {

        DocumentPermission permission = new DocumentPermission();
        permission.setDocument(document);
        permission.setUser(user);
        permission.setPermissionType(permissionType);

        permissionRepository.save(permission);
    }
    // ===============================
// CHECK PERMISSION
// ===============================
    public boolean hasAccess(Document document,
                             User user,
                             PermissionType required) {

        // Public document → everyone can VIEW
        if (document.isPublic() && required == PermissionType.VIEW) {
            return true;
        }

        // Owner has full access
        if (document.getOwner().getId().equals(user.getId())) {
            return true;
        }


        // If EDIT required → must have EDIT permission
        if (required == PermissionType.EDIT) {
            return permissionRepository
                    .findByUserAndDocumentAndPermissionType(
                            user, document, PermissionType.EDIT)
                    .isPresent();
        }

        // If VIEW required → VIEW or EDIT is enough
        return permissionRepository
                .findByUserAndDocumentAndPermissionType(
                        user, document, PermissionType.VIEW)
                .isPresent()
                ||
                permissionRepository
                        .findByUserAndDocumentAndPermissionType(
                                user, document, PermissionType.EDIT)
                        .isPresent();
    }

    public String extractTextFromFile(String filePath) {
        try {
            File file = new File(filePath);

            if (!file.exists()) {
                return "File not found";
            }

            // TXT files
            if (filePath.endsWith(".txt")) {
                return Files.readString(file.toPath());
            }

            // PDF files
            if (filePath.endsWith(".pdf")) {
                PDDocument document = PDDocument.load(file);
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                document.close();
                return text;
            }

            return "Preview not supported for this file type";

        } catch (Exception e) {
            return "Error extracting text: " + e.getMessage();
        }
    }

    public String getDocumentText(Long documentId) {

        DocumentVersion currentVersion = documentVersionRepository
                .findByDocumentId(documentId)
                .stream()
                .filter(DocumentVersion::isCurrent)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No current version found"));

        String filePath = currentVersion.getFilePath();

        String text = extractTextFromFile(filePath);

        if (text == null || text.isEmpty()) {
            return "No readable text found in document.";
        }

        // IMPORTANT: Limit text for AI performance
        int maxLength = 1500;
        if (text.length() > maxLength) {
            text = text.substring(0, maxLength);
        }

        return text;
    }

    public List<Document> searchDocuments(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return documentRepository.findAll();
        }

        return documentRepository
                .findByTitleContainingIgnoreCaseOrTagsContainingIgnoreCase(keyword, keyword);
    }

    public double getTotalStorageUsed() {

        List<Document> documents = documentRepository.findAll();

        long totalBytes = documents.stream()
                .mapToLong(Document::getFileSize)
                .sum();

        double totalMB = totalBytes / (1024.0 * 1024.0);

        return Math.round(totalMB * 100.0) / 100.0; // round to 2 decimals
    }

    public List<DocumentTypeStatsDTO> getDocumentTypeStats() {

        List<Object[]> results = documentRepository.countDocumentsByType();

        List<DocumentTypeStatsDTO> stats = new ArrayList<>();

        for (Object[] row : results) {

            String mimeType = (String) row[0];
            Long count = (Long) row[1];

            String type;

            if (mimeType.contains("pdf")) {
                type = "PDF";
            }
            else if (mimeType.contains("wordprocessingml")) {
                type = "DOCX";
            }
            else if (mimeType.contains("text")) {
                type = "TXT";
            }
            else {
                type = "OTHER";
            }

            stats.add(new DocumentTypeStatsDTO(type, count));
        }

        return stats;
    }

    public String askAI(Long documentId, String question) {

        return aiService.askQuestion(documentId, question);
    }


}
