package com.edms.edms.repository;

import com.edms.edms.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.edms.edms.entity.User;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByTitleContainingIgnoreCase(String title);
    List<Document> findByFolderId(Long folderId);
    boolean existsByFolderIdAndTitle(Long folderId, String title);
    List<Document> findByOwner(User owner);
    List<Document> findByIsPublicTrue();
    List<Document> findByFolderIdAndOwner(Long folderId, User owner);
    List<Document> findByTitleContainingIgnoreCaseOrTagsContainingIgnoreCase(String title, String tags);
    @Query("SELECT d.fileType, COUNT(d) FROM Document d GROUP BY d.fileType")
    List<Object[]> countDocumentsByType();

}
