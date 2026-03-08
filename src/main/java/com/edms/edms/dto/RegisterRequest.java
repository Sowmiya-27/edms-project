package com.edms.edms.dto;
import com.edms.edms.entity.Role;

public class RegisterRequest {

    private String name;
    private String email;
    private String department;
    private String password;
    private Role role;

    // ========================
    // GETTERS
    // ========================

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartment() {
        return department;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
}
