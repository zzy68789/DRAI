package com.zzy.drai.controller;

import com.zzy.drai.auth.UserContext;
import com.zzy.drai.dto.ChatRequest;
import com.zzy.drai.service.ResearchTaskService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api")
public class ChatController {
    private final ResearchTaskService researchTaskService;
    private final UserContext userContext;

    public ChatController(ResearchTaskService researchTaskService, UserContext userContext) {
        this.researchTaskService = researchTaskService;
        this.userContext = userContext;
    }

    @PostMapping("/chat")
    public SseEmitter chat(@Valid @RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        researchTaskService.run(userContext.currentUserId(), request, emitter);
        return emitter;
    }
}
