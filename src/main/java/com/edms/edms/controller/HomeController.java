package com.edms.edms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/documents-ui")
    public String documentsPage() {
        return "documents"; // documents.html
    }

    @GetMapping("/admin-ui")
    public String adminPage() {
        return "admin"; // admin.html
    }

    @GetMapping("/auth")
    public String authPage() {
        return "auth"; // auth.html
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
}
