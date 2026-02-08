package com.edms.edms.service;

import org.springframework.stereotype.Service;

import com.edms.edms.entity.Document;
import com.edms.edms.entity.DocumentPermission;
import com.edms.edms.entity.PermissionType;
import com.edms.edms.entity.User;
import com.edms.edms.repository.DocumentPermissionRepository;

@Service
public class DocumentPermissionService {

    private final DocumentPermissionRepository permissionRepository;

    public DocumentPermissionService(DocumentPermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    // ===============================
    // GRANT PERMISSION
    // ===============================
    public void grantPermission(User user, Document document, PermissionType permissionType) {

        // Check if permission already exists
        boolean exists = permissionRepository
                .findByUserAndDocumentAndPermissionType(user, document, permissionType)
                .isPresent();

        if (!exists) {
            DocumentPermission permission = new DocumentPermission();
            permission.setUser(user);
            permission.setDocument(document);
            permission.setPermissionType(permissionType);

            permissionRepository.save(permission);
        }
    }

    // ===============================
    // CHECK VIEW PERMISSION
    // ===============================
    public boolean canView(User user, Document document) {
        if (document.getOwner() == null) {
            return true;
        }
        return document.getOwner().getId().equals(user.getId());
    }


    // ===============================
    // CHECK EDIT PERMISSION
    // ===============================
    public boolean canEdit(User user, Document document) {

        // Owner can always edit
        if (document.getOwner().getId().equals(user.getId())) {
            return true;
        }

        return permissionRepository
                .findByUserAndDocumentAndPermissionType(user, document, PermissionType.EDIT)
                .isPresent();
    }
}
