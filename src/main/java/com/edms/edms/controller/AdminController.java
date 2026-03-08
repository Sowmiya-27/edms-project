package com.edms.edms.controller;

import com.edms.edms.dto.AdminDocumentDTO;
import com.edms.edms.dto.AdminOverviewDTO;
import com.edms.edms.dto.DocumentTypeStatsDTO;
import com.edms.edms.entity.Document;
import com.edms.edms.entity.User;
import com.edms.edms.service.AdminService;

import com.edms.edms.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private DocumentService documentService;

    // =========================
    // 1. OVERVIEW
    // =========================
    @GetMapping("/overview")
    public AdminOverviewDTO getOverview() {
        return adminService.getOverview();
    }

    // =========================
    // 2. ALL USERS
    // =========================
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return adminService.getAllUsers();
    }

    // =========================
    // 3. ALL DOCUMENTS
    // =========================
    @GetMapping("/documents")
    public List<AdminDocumentDTO> getAllDocuments() {
        return adminService.getAllDocuments();
    }

    // =========================
    // 4. AI LOGS
    // =========================
    @GetMapping("/ai-logs")
    public Object getAILogs() {
        return adminService.getAILogs();
    }

    // =========================
    // 5. CHANGE ROLE
    // =========================
    @PutMapping("/change-role/{id}")
    public String changeUserRole(@PathVariable Long id) {
        adminService.changeUserRole(id);
        return "Role Updated";
    }

    // =========================
    // 6. DELETE DOCUMENT
    // =========================
    @DeleteMapping("/delete-document/{id}")
    public String deleteDocument(@PathVariable Long id) {
        adminService.deleteDocument(id);
        return "Document Deleted";
    }

    @GetMapping("/document-stats")
    public List<DocumentTypeStatsDTO> getDocumentStats() {
        return documentService.getDocumentTypeStats();
    }
}
