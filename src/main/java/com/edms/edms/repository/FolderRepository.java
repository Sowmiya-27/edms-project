package com.edms.edms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.edms.edms.entity.Folder;

public interface FolderRepository extends JpaRepository<Folder, Long> {
}
