package com.edms.edms.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "document_permissions")
public class DocumentPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===============================
    // USER
    // ===============================
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ===============================
    // DOCUMENT
    // ===============================
    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    // ===============================
    // PERMISSION TYPE (VIEW / EDIT)
    // ===============================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionType permissionType;

    // ===============================
    // GETTERS & SETTERS
    // ===============================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public PermissionType getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(PermissionType permissionType) {
        this.permissionType = permissionType;
    }
}
