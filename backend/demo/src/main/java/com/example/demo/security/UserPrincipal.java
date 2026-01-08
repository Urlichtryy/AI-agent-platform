package com.example.demo.security;

public class UserPrincipal {
    private final Long id;
    private final String username;

    public UserPrincipal(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
}
