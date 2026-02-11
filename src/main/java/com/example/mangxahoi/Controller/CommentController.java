package com.example.mangxahoi.Controller;

import com.example.mangxahoi.DTO.Request.CommentRequest;
import com.example.mangxahoi.DTO.Response.CommentResponse;
import com.example.mangxahoi.DTO.Response.DeleteCommentResponse;
import com.example.mangxahoi.Enums.CommentTargetType;
import com.example.mangxahoi.Service.CommentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommentResponse addOrUpdate(
            @RequestPart("data") CommentRequest commentRequest,
            @RequestPart(value = "image",required = false) MultipartFile image,
            Authentication authentication) throws IOException {
        return commentService.createOrUpdateComment(commentRequest,image, authentication.getName());
    }
    @GetMapping
    public List<CommentResponse> get10CommentDesc(@RequestParam CommentTargetType commentTargetType,
                                                  @RequestParam Long targetId,
                                                  @RequestParam int page,
                                                  Authentication authentication
                                                  )
    {
        return commentService.getComments(commentTargetType, targetId, page, authentication.getName());
    }

    //lấy ra reply
    @GetMapping("/replies")
    public List<CommentResponse> get10RepliesDesc(
            @RequestParam Long parentId,
            @RequestParam int page,
            Authentication authentication
    ){
        return commentService.getReplies(parentId, page, authentication.getName());
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<DeleteCommentResponse> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                commentService.deleteComment(commentId, authentication.getName())
        );
    }

}
