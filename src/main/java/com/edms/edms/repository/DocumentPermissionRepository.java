package com.edms.edms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edms.edms.entity.Document;
import com.edms.edms.entity.DocumentPermission;
import com.edms.edms.entity.PermissionType;
import com.edms.edms.entity.User;

public interface DocumentPermissionRepository
        extends JpaRepository<DocumentPermission, Long> {

    Optional<DocumentPermission> findByUserAndDocumentAndPermissionType(
            User user,
            Document document,
            PermissionType permissionType
    );
    List<DocumentPermission> findByDocumentId(Long documentId);
    List<DocumentPermission> findByUser(User user);
}
