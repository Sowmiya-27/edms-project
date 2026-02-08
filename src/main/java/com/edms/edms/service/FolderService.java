package com.edms.edms.service;

import org.springframework.stereotype.Service;
import com.edms.edms.entity.Folder;
import com.edms.edms.repository.FolderRepository;

import java.util.List;

@Service
public class FolderService {

    private final FolderRepository folderRepository;

    public FolderService(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    public Folder createFolder(Folder folder) {
        return folderRepository.save(folder);
    }

    public List<Folder> getAllFolders() {
        return folderRepository.findAll();
    }

    public Folder getFolderById(Long id) {
        return folderRepository.findById(id).orElseThrow();
    }
}
