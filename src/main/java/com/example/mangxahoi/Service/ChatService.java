package com.example.mangxahoi.Service;

import com.example.mangxahoi.DTO.Chat.ConversationResponse;
import com.example.mangxahoi.DTO.Chat.CursorPage;
import com.example.mangxahoi.DTO.Chat.MessageRes;
import com.example.mangxahoi.DTO.Chat.UploadRes;
import com.example.mangxahoi.Entity.ConversationEntity;
import com.example.mangxahoi.Entity.MessageEntity;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Repository.ConversationRepository;
import com.example.mangxahoi.Repository.MessageRepository;
import com.example.mangxahoi.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ImageService imageService;
    @Value("${app.upload.dir}")
    private String uploadDir;

    public ChatService(ConversationRepository conversationRepository, UserRepository userRepository, MessageRepository messageRepository, ImageService imageService) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.imageService = imageService;
    }

    //hàm xử lí ảnh trong đoaạn chat
    public UploadRes uploadChatImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File rỗng");
        }
        String folder = Paths.get(uploadDir, "chat").toString();
        Files.createDirectories(Paths.get(folder));

        String original = Paths.get(file.getOriginalFilename()).getFileName().toString();
        String fileName = UUID.randomUUID() + "_" + original;

        Path filePath = Paths.get(folder, fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return new UploadRes("/chat/" + fileName);
    }

    //kiểm tra xem đoạn chat đã tồn tại chưa, nếu rồi thì trả về đoạn chat, nếu chưa thì trả null
    @Transactional(readOnly = true)
    public CursorPage<MessageRes> getMessagesOrNull(String meUsername, Long otherUserId, Long beforeId, int size) {

        UserEntity me = userRepository.findByUsername(meUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (me.getId().equals(otherUserId)) {
            throw new IllegalArgumentException("không chat với chính mình");
        }

        UserEntity other = userRepository.findById(otherUserId)
                .orElseThrow(() -> new IllegalArgumentException("Other user not found"));

        ConversationEntity conversation =
                conversationRepository.lookUpConversation(me.getId(), other.getId()).orElse(null);

        if (conversation == null) return null;

        int limit = Math.min(size, 50);
        Pageable pageable = PageRequest.of(0, limit);

        List<MessageEntity> list = (beforeId == null)
                ? messageRepository.findByConversationIdOrderByIdDesc(conversation.getId(), pageable)
                : messageRepository.findByConversationIdAndIdLessThanOrderByIdDesc(conversation.getId(), beforeId, pageable);

        Collections.reverse(list);

        Long nextCursor = list.isEmpty() ? null : list.get(0).getId();
        boolean hasMore = list.size() == limit;

        List<MessageRes> items = list.stream().map(m -> new MessageRes(
                m.getId(),
                conversation.getId(),
                m.getSender().getId(),
                m.getContentText(),
                m.getImageUrl(),
                m.getCreatedAt()
        )).toList();

        return new CursorPage<>(items, nextCursor, hasMore);
    }

    //lấy ra các đoạn chat
    public List<ConversationResponse> getAllConversations(String username) {
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<ConversationEntity> conversationEntities = conversationRepository.getAllConversations(user.getId());

        //lấy ra tin nhắn mới nhất

        return conversationEntities.stream().map(c -> {
            UserEntity recipient = user.getId().equals(c.getUser1().getId())
                    ? c.getUser2()
                    : c.getUser1();

            ConversationResponse conversationResponse = new ConversationResponse(
                    recipient.getId(),
                    imageService.buildImageUrl(recipient.getAvatar()),
                    recipient.getFullName(),

                    c.getLastMessage(),
                    c.getLastSenderId(),
                    c.getLastMessageAt(),
                    c.getLastMessageType()
            );
            return conversationResponse;
        }).toList();
    }
}
