package com.edms.edms.dto;

import com.edms.edms.entity.Document;

public class AdminDocumentDTO {

    private Long id;
    private String title;
    private String ownerName;
    private String folderName;
    private String fileType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public AdminDocumentDTO(Document doc) {
        this.id = doc.getId();
        this.title = doc.getTitle();
        this.ownerName = doc.getOwner() != null
                ? doc.getOwner().getName()
                : "N/A";
        this.folderName = doc.getFolder() != null ? doc.getFolder().getName() : "N/A";
        this.fileType = doc.getFileType();
    }


}
