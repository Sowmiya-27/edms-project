package com.edms.edms.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users") // 'user' is reserved in some DBs
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String department;

    @Column(nullable = false)
    private String password;

    // ===============================
    // GETTERS & SETTERS
    // ===============================

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPassword() {
        return password;
    }

    // ⚠️ Later we will encrypt this
    public void setPassword(String password) {
        this.password = password;
    }
}
