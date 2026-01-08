package com.example.demo.dto.user;

import com.example.demo.entity.User;

public class LoginResponse {
    private String token;
    private long expiresAt;
    private SimpleUser user;

    public static class SimpleUser {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
    }

    public static SimpleUser fromUser(User u) {
        SimpleUser su = new SimpleUser();
        su.setId(u.getId());
        su.setUsername(u.getUsername());
        su.setNickname(u.getNickname());
        su.setAvatar(u.getAvatar());
        return su;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
    public SimpleUser getUser() { return user; }
    public void setUser(SimpleUser user) { this.user = user; }
}
