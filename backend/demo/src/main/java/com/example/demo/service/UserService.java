package com.example.demo.service;

import com.example.demo.dto.user.UpdateProfileRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional
    public User register(String username, String rawPassword, String email, String phone) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setEmail(email);
        u.setPhone(phone);
        u.setStatus(1);
        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(u);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean checkPassword(User u, String rawPassword) {
        return passwordEncoder.matches(rawPassword, u.getPassword());
    }

    public Optional<User> findById(Long id) { return userRepository.findById(id); }

    @Transactional
    public User updateProfile(User u, UpdateProfileRequest req) {
        if (req.getNickname() != null) u.setNickname(req.getNickname());
        if (req.getEmail() != null) u.setEmail(req.getEmail());
        if (req.getPhone() != null) u.setPhone(req.getPhone());
        if (req.getAvatar() != null) u.setAvatar(req.getAvatar());
        u.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(u);
    }

    @Transactional
    public void changePassword(User u, String newRawPassword) {
        u.setPassword(passwordEncoder.encode(newRawPassword));
        u.setUpdatedAt(LocalDateTime.now());
        userRepository.save(u);
    }
}
