package com.example.demo.service;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.ChatSession;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.ChatSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final LlmService llmService;

    public ChatService(ChatSessionRepository chatSessionRepository,
                       ChatMessageRepository chatMessageRepository,
                       LlmService llmService) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.llmService = llmService;
    }

    @Transactional
    public ChatSession createSession(Long userId, String title) {
        ChatSession s = new ChatSession();
        s.setUserId(userId);
        s.setTitle(title != null && !title.isBlank() ? title : "新会话");
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());
        s.setIsDeleted(0);
        return chatSessionRepository.save(s);
    }

    public List<ChatSession> listSessions(Long userId) {
        return chatSessionRepository.findByUserIdAndIsDeletedOrderByUpdatedAtDesc(userId, 0);
    }

    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        chatSessionRepository.findById(sessionId).ifPresent(s -> {
            if (Objects.equals(s.getUserId(), userId)) {
                s.setIsDeleted(1);
                s.setUpdatedAt(LocalDateTime.now());
                chatSessionRepository.save(s);
            }
        });
    }

    public List<ChatMessage> listMessages(Long sessionId) {
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Transactional
    public String sendMessage(Long userId, Long sessionId, String content) {
        ChatSession s = chatSessionRepository.findById(sessionId).orElseThrow();
        if (!Objects.equals(s.getUserId(), userId)) {
            throw new RuntimeException("会话不存在或无权限");
        }
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(content);
        userMsg.setCreatedAt(LocalDateTime.now());
        chatMessageRepository.save(userMsg);

        List<ChatMessage> history = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        List<Map<String, String>> messages = history.stream()
                .map(m -> {
                    Map<String, String> x = new HashMap<>();
                    x.put("role", m.getRole());
                    x.put("content", m.getContent());
                    return x;
                })
                .collect(Collectors.toList());
        String reply = llmService.chat("qwen-max", messages);

        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setRole("assistant");
        aiMsg.setContent(reply);
        aiMsg.setCreatedAt(LocalDateTime.now());
        chatMessageRepository.save(aiMsg);

        s.setUpdatedAt(LocalDateTime.now());
        chatSessionRepository.save(s);

        return reply;
    }
}
