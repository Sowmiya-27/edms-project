package com.edms.edms.controller;

import org.springframework.web.bind.annotation.*;
import com.edms.edms.entity.Folder;
import com.edms.edms.service.FolderService;

import java.util.List;

@RestController
@RequestMapping("/folders")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping
    public Folder createFolder(@RequestBody Folder folder) {
        return folderService.createFolder(folder);
    }

    @GetMapping
    public List<Folder> getFolders() {
        return folderService.getAllFolders();
    }
}
