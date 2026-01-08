package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.chat.CreateSessionRequest;
import com.example.demo.dto.chat.SendMessageRequest;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.ChatSession;
import com.example.demo.security.SecurityUtils;
import com.example.demo.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/session")
    public ResponseEntity<ApiResponse<ChatSession>> createSession(@RequestBody CreateSessionRequest req) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) return ResponseEntity.status(401).body(ApiResponse.error("未授权"));
        ChatSession s = chatService.createSession(uid, req.getTitle());
        return ResponseEntity.ok(ApiResponse.success(s));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<ChatSession>>> listSessions() {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) return ResponseEntity.status(401).body(ApiResponse.error("未授权"));
        return ResponseEntity.ok(ApiResponse.success(chatService.listSessions(uid)));
    }

    @DeleteMapping("/session/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteSession(@PathVariable("id") Long id) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) return ResponseEntity.status(401).body(ApiResponse.error("未授权"));
        chatService.deleteSession(uid, id);
        return ResponseEntity.ok(ApiResponse.successMsg("删除成功"));
    }

    @GetMapping("/messages/{sessionId}")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> listMessages(@PathVariable Long sessionId) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) return ResponseEntity.status(401).body(ApiResponse.error("未授权"));
        return ResponseEntity.ok(ApiResponse.success(chatService.listMessages(sessionId)));
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Object>> send(@RequestBody SendMessageRequest req) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) return ResponseEntity.status(401).body(ApiResponse.error("未授权"));
        if (req.getSessionId() == null || req.getSessionId() <= 0 || req.getContent() == null || req.getContent().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("参数不合法"));
        }
        String reply = chatService.sendMessage(uid, req.getSessionId(), req.getContent());
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of("reply", reply)));
    }
}
