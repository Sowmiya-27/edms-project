package com.edms.edms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.edms.edms.entity.DocumentVersion;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    List<DocumentVersion> findByDocumentId(Long documentId);

}
