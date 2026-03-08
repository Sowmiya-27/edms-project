package com.edms.edms.service;

import com.edms.edms.dto.AdminDocumentDTO;
import com.edms.edms.dto.AdminOverviewDTO;
import com.edms.edms.entity.User;
import com.edms.edms.repository.DocumentRepository;
import com.edms.edms.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.edms.edms.entity.Role;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentService documentService;

    // =========================
    // OVERVIEW DATA
    // =========================
    public AdminOverviewDTO getOverview() {

        long totalUsers = userRepository.count();
        long totalDocuments = documentRepository.count();
        double storageUsed = documentService.getTotalStorageUsed();

        AdminOverviewDTO dto = new AdminOverviewDTO();
        dto.setTotalUsers(totalUsers);
        dto.setTotalDocuments(totalDocuments);
        dto.setTotalQueries(0);
        dto.setStorageUsed(storageUsed);

        return dto;
    }

    // =========================
    // GET ALL USERS
    // =========================
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // =========================
    // GET ALL DOCUMENTS
    // =========================
    public List<AdminDocumentDTO> getAllDocuments() {
        return documentRepository.findAll()
                .stream()
                .map(AdminDocumentDTO::new)
                .toList();
    }

    // =========================
    // CHANGE ROLE
    // =========================
    public void changeUserRole(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            user.setRole(Role.USER);
        } else {
            user.setRole(Role.ADMIN);
        }

        userRepository.save(user);
    }

    // =========================
    // DELETE DOCUMENT
    // =========================
    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
    }

    // =========================
    // AI LOGS (TEMP)
    // =========================
    public Object getAILogs() {
        return List.of();
    }
}