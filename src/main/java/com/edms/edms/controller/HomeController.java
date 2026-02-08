package com.edms.edms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/documents-ui")
    public String documentsPage() {
        return "documents"; // documents.html
    }
}
