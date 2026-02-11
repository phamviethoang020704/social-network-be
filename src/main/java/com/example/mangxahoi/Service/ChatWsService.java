package com.example.mangxahoi.Service;

import com.example.mangxahoi.DTO.Chat.ChatMessageResponse;
import com.example.mangxahoi.DTO.Chat.ChatSendRequest;
import com.example.mangxahoi.Entity.ConversationEntity;
import com.example.mangxahoi.Entity.MessageEntity;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Enums.MessageType;
import com.example.mangxahoi.Repository.ConversationRepository;
import com.example.mangxahoi.Repository.MessageRepository;
import com.example.mangxahoi.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatWsService {
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ImageService imageService;


    @Transactional
    public ChatMessageResponse sendMessage(String fromUsername, ChatSendRequest request) {
        System.out.println("WS request = " + request);
        UserEntity from = userRepository.findByUsername(fromUsername)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        UserEntity to = userRepository.findById(request.toUserId())
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));
        if (from.getId().equals(to.getId())) throw new IllegalArgumentException("không nhắn cho chính mình");

        String text = request.text();
        if (text != null) {
            text = text.trim();
            if (text.isEmpty()) text = null;
        }

        String imageUrl = request.imageUrl();
        if (imageUrl != null) {
            imageUrl = imageUrl.trim();
            if (imageUrl.isEmpty()) imageUrl = null;
        }

        if (text == null && imageUrl == null) {
            throw new IllegalArgumentException("tin nhắn phải có text hoặc ảnh");
        }



        ConversationEntity conversation = findOrCreateConversation(from, to);

        MessageEntity message = new MessageEntity();
        message.setConversation(conversation);
        message.setSender(from);
        message.setContentText(text);
        message.setImageUrl(imageUrl);

        MessageEntity saved = messageRepository.save(message);

        conversation.setLastMessageAt(LocalDateTime.now());
        conversation.setLastMessage(text);
        conversation.setLastSenderId(request.toUserId());
        conversation.setLastMessageType(imageUrl != null ? MessageType.IMAGE : MessageType.TEXT);
        conversationRepository.save(conversation);

        return new ChatMessageResponse(
                saved.getId(),
                conversation.getId(),
                from.getId(),
                to.getId(),
                saved.getContentText(),
                saved.getImageUrl(),
                saved.getCreatedAt()
        );
    }

    //userB sẽ có id > userA
    private ConversationEntity findOrCreateConversation(UserEntity userA, UserEntity userB){
        Long aId = userA.getId();
        Long bId = userB.getId();

        UserEntity user1 = aId <= bId ? userA : userB;
        UserEntity user2 = aId <= bId ? userB : userA;

        return conversationRepository.findByUser1IdAndUser2Id(user1.getId(), user2.getId())
                .orElseGet(() -> {
                    ConversationEntity conversation = new ConversationEntity();
                    conversation.setUser1(user1);
                    conversation.setUser2(user2);
                    return conversationRepository.save(conversation);
                });
    }
}
