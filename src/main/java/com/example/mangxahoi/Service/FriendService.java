package com.example.mangxahoi.Service;

import com.example.mangxahoi.DTO.Response.FriendResponse;
import com.example.mangxahoi.Entity.FriendEntity;
import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Enums.FriendStatus;
import com.example.mangxahoi.Repository.FriendRepository;
import com.example.mangxahoi.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;

    public FriendStatus getStatus(UserEntity viewer, UserEntity owner) {

        Optional<FriendEntity> relation =
                friendRepository.findByUserSendAndUserAcceptOrUserSendAndUserAccept(
                        viewer, owner,
                        owner, viewer
                );

        if (relation.isEmpty()) return FriendStatus.NONE;

        FriendEntity f = relation.get();

        if (!f.isAccept()) {
            return f.getUserSend().equals(viewer)
                    ? FriendStatus.SENT
                    : FriendStatus.RECEIVED;
        }

        return FriendStatus.FRIEND;
    }

    public void send(UserEntity sender, UserEntity receiver) {
        if (friendRepository
                .findByUserSendAndUserAcceptOrUserSendAndUserAccept(
                        sender, receiver,
                        receiver, sender
                ).isPresent()) return;

        FriendEntity f = new FriendEntity();
        f.setUserSend(sender);
        f.setUserAccept(receiver);
        f.setAccept(false);

        friendRepository.save(f);
    }

    public void accept(UserEntity sender, UserEntity receiver) {
        friendRepository.findByUserSendAndUserAccept(sender, receiver)
                .ifPresent(f -> {
                    f.setAccept(true);
                    friendRepository.save(f);
                });
    }

    public void cancel(UserEntity u1, UserEntity u2) {
        friendRepository
                .findByUserSendAndUserAcceptOrUserSendAndUserAccept(
                        u1, u2,
                        u2, u1
                ).ifPresent(friendRepository::delete);
    }

    //tìm user đã kết bạn theo fullName
    public List<FriendResponse> searchFriendsByFullName(String username, String keyword){
        UserEntity userEntity = userRepository.findByUsername(username).orElseThrow(
                () -> new RuntimeException("user not found")
        );
        List<FriendEntity> friends =
                friendRepository.searchAcceptedFriendsByFullName(userEntity.getId(), keyword);
        return friends.stream().map(
                friend -> {
                    UserEntity friendUser = friend.getUserSend().getId().equals(userEntity.getId())
                            ? friend.getUserAccept()
                            : friend.getUserSend();
                    return new FriendResponse(
                            friendUser.getId(),
                            friendUser.getFullName(),
                            imageService.buildImageUrl(friendUser.getAvatar())
                    );
                }).distinct().toList();


    }
}
