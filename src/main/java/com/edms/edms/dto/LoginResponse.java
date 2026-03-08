package com.edms.edms.dto;

import com.edms.edms.entity.Role;
import com.edms.edms.entity.User;

public class LoginResponse {

    private Long id;
    private String name;
    private String email;
    private String department;
    private Role role;

    public LoginResponse(Long id, String name, String email, String department, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }
    public Role getRole() { return role; }
}
