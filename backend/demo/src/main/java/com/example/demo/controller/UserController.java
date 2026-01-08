package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.user.*;
import com.example.demo.entity.User;
import com.example.demo.security.JwtService;
import com.example.demo.security.SecurityUtils;
import com.example.demo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(@RequestBody RegisterRequest req) {
        if (req.getUsername() == null || req.getUsername().length() < 3 || req.getUsername().length() > 50) {
            return ResponseEntity.badRequest().body(ApiResponse.error("参数不合法"));
        }
        if (req.getPassword() == null || req.getPassword().length() < 6 || req.getPassword().length() > 64) {
            return ResponseEntity.badRequest().body(ApiResponse.error("参数不合法"));
        }
        if (userService.usernameExists(req.getUsername())) {
            return ResponseEntity.status(409).body(ApiResponse.error("用户名已存在"));
        }
        userService.register(req.getUsername(), req.getPassword(), req.getEmail(), req.getPhone());
        return ResponseEntity.ok(ApiResponse.successMsg("注册成功"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(@RequestBody LoginRequest req) {
        return userService.findByUsername(req.getUsername())
                .map(u -> {
                    if (u.getStatus() != null && u.getStatus() == 0) {
                        return ResponseEntity.status(423).body(ApiResponse.error("用户被禁用"));
                    }
                    if (!userService.checkPassword(u, req.getPassword())) {
                        return ResponseEntity.status(401).body(ApiResponse.error("用户名或密码错误"));
                    }
                    String token = jwtService.generateToken(u.getId(), u.getUsername());
                    LoginResponse lr = new LoginResponse();
                    lr.setToken(token);
                    long exp = Instant.now().toEpochMilli() + 1000L * 60 * 60 * 24 * 7; // 对齐配置时长近似
                    lr.setExpiresAt(exp);
                    lr.setUser(LoginResponse.fromUser(u));
                    return ResponseEntity.ok(ApiResponse.success((Object) lr));
                })
                .orElseGet(() -> ResponseEntity.status(401).body(ApiResponse.error("用户名或密码错误")));
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Object>> info() {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) return ResponseEntity.status(401).body(ApiResponse.error("未授权"));
        return userService.findById(uid)
            .<ResponseEntity<ApiResponse<Object>>>map(u -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", u.getId());
                data.put("username", u.getUsername());
                data.put("nickname", u.getNickname());
                data.put("email", u.getEmail());
                data.put("phone", u.getPhone());
                data.put("avatar", u.getAvatar());
                data.put("status", u.getStatus());
                data.put("lastLoginTime", u.getLastLoginTime());
                data.put("createdAt", u.getCreatedAt());
                return ResponseEntity.ok(ApiResponse.success((Object) data));
            })
                .orElseGet(() -> ResponseEntity.status(401).body(ApiResponse.error("未授权")));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Object>> updateProfile(@RequestBody UpdateProfileRequest req) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) return ResponseEntity.status(401).body(ApiResponse.error("未授权"));
        if ((req.getEmail() != null && !StringUtils.hasText(req.getEmail())) ||
            (req.getPhone() != null && !StringUtils.hasText(req.getPhone()))) {
            return ResponseEntity.badRequest().body(ApiResponse.error("参数不合法"));
        }
        return userService.findById(uid)
            .map(u -> ResponseEntity.ok(ApiResponse.success((Object) userService.updateProfile(u, req))))
                .orElseGet(() -> ResponseEntity.status(401).body(ApiResponse.error("未授权")));
    }

    @PostMapping("/password/change")
    public ResponseEntity<ApiResponse<Object>> changePassword(@RequestBody ChangePasswordRequest req) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) return ResponseEntity.status(401).body(ApiResponse.error("未授权"));
        if (req.getNewPassword() == null || req.getNewPassword().length() < 6) {
            return ResponseEntity.badRequest().body(ApiResponse.error("新密码不合法"));
        }
        return userService.findById(uid)
                .map(u -> {
                    if (!passwordEncoder.matches(req.getOldPassword(), u.getPassword())) {
                        return ResponseEntity.status(403).body(ApiResponse.error("旧密码不正确"));
                    }
                    userService.changePassword(u, req.getNewPassword());
                    return ResponseEntity.ok(ApiResponse.successMsg("修改成功"));
                })
                .orElseGet(() -> ResponseEntity.status(401).body(ApiResponse.error("未授权")));
    }
}
