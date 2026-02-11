package com.example.mangxahoi.Controller;

import com.example.mangxahoi.DTO.Chat.ConversationResponse;
import com.example.mangxahoi.DTO.Chat.CursorPage;
import com.example.mangxahoi.DTO.Chat.MessageRes;
import com.example.mangxahoi.DTO.Chat.UploadRes;
import com.example.mangxahoi.Service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/upload")
    public UploadRes upload(@RequestParam("file") MultipartFile file) throws IOException {
        return chatService.uploadChatImage(file);
    }


    @GetMapping("/conversations/{otherUserId}/messages")
    public ResponseEntity<CursorPage<MessageRes>> getMessagesOrNull(
            @PathVariable Long otherUserId,
            @RequestParam(required = false) Long beforeId,
            @RequestParam(defaultValue = "20") int size,
            Principal principal
    ) {
        String me = principal.getName();
        CursorPage<MessageRes> page = chatService.getMessagesOrNull(me, otherUserId, beforeId, size);
        if (page == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(page);
    }

    @GetMapping("/conversations")
    public List<ConversationResponse> getAllConversations(Principal principal) {
        return chatService.getAllConversations(principal.getName());
    }
}