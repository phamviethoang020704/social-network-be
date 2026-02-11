package com.example.mangxahoi.Controller;

import com.example.mangxahoi.DTO.Chat.ChatMessageResponse;
import com.example.mangxahoi.DTO.Chat.ChatSendRequest;
import com.example.mangxahoi.DTO.Chat.TypingEventRequest;
import com.example.mangxahoi.DTO.Chat.TypingEventResponse;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Repository.UserRepository;
import com.example.mangxahoi.Service.ChatWsService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWsController {
    private final ChatWsService chatService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserRepository userRepository;

    public ChatWsController(ChatWsService chatService, SimpMessagingTemplate simpMessagingTemplate, UserRepository userRepository) {
        this.chatService = chatService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.userRepository = userRepository;
    }

    @MessageMapping("/chat.send")
    public void send(ChatSendRequest request, Principal principal) {
        String fromUsername = principal.getName();

        ChatMessageResponse res = chatService.sendMessage(fromUsername, request);

        UserEntity from = userRepository.findByUsername(fromUsername).orElseThrow(() -> new RuntimeException("user not found"));
        UserEntity to = userRepository.findById(request.toUserId()).orElseThrow(() -> new RuntimeException("user not found"));

        simpMessagingTemplate.convertAndSendToUser(from.getUsername(), "/queue/messages", res);
        simpMessagingTemplate.convertAndSendToUser(to.getUsername(), "/queue/messages", res);
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload TypingEventRequest request, Principal principal) {
        String fromUsername = principal.getName();

        UserEntity from = userRepository.findByUsername(fromUsername).orElseThrow(() -> new RuntimeException("user not found"));
        UserEntity to = userRepository.findById(request.toUserId()).orElseThrow(() -> new RuntimeException("user not found"));

        boolean typing = Boolean.TRUE.equals(request.typing());

        TypingEventResponse response = new TypingEventResponse(
                from.getId(),
                typing
        );

        simpMessagingTemplate.convertAndSendToUser(
                to.getUsername(),
                "/queue/typing",
                response
        );
    }
}
