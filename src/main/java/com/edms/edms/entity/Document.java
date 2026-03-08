package com.edms.edms.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileType;
    private long fileSize;
    private String filePath;
    private String title;

    // ===============================
    // PUBLIC / PRIVATE
    // ===============================
    @Column(nullable = false)
    private boolean isPublic = false; // default PRIVATE

    @Column(name="tags")
    private String tags;

    // ===============================
    // RELATIONSHIP : FOLDER
    // ===============================
    @ManyToOne
    @JoinColumn(name = "folder_id")
    @JsonIgnore
    private Folder folder;

    // ===============================
    // RELATIONSHIP : DOCUMENT VERSIONS
    // ===============================
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentVersion> versions;

    // ===============================
// OWNER (RELATIONSHIP WITH USER)
// ===============================
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // ===============================
// SHARED USERS
// ===============================
    @ManyToMany
    @JoinTable(
            name = "document_shared_users",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private java.util.Set<User> sharedUsers = new java.util.HashSet<>();

    // ===============================
    // GETTERS & SETTERS
    // ===============================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public List<DocumentVersion> getVersions() {
        return versions;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public java.util.Set<User> getSharedUsers() {
        return sharedUsers;
    }

    public void setSharedUsers(java.util.Set<User> sharedUsers) {
        this.sharedUsers = sharedUsers;
    }

    public void setVersions(List<DocumentVersion> versions) {
        this.versions = versions;
    }

}
