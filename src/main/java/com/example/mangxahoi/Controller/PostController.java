package com.example.mangxahoi.Controller;

import com.example.mangxahoi.DTO.Request.PostRequest;
import com.example.mangxahoi.DTO.EditContent;
import com.example.mangxahoi.DTO.Response.PostSliceResponse;
import com.example.mangxahoi.Service.PostService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createOrUpdatePost(
            @RequestPart("data") PostRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> files
    ) throws IOException {

        Object result = postService.createOrUpdatePost(request, files);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        postService.deletePost(postId, username);

        return ResponseEntity.noContent().build(); // 204
    }

    @GetMapping("/feed/{groupId}")
    public ResponseEntity<PostSliceResponse> getAllFeed(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorTime,
            @RequestParam(required = false) Long cursorId
    ) {
        return ResponseEntity.ok(postService.getPostFromGroupSlice(groupId, size, cursorTime, cursorId));
    }

    @PostMapping("/edit-content")
    public ResponseEntity<EditContent> editContent(@RequestBody EditContent request, Authentication authentication) {
        return ResponseEntity.ok(postService.editContent(request,authentication.getName()));
    }

    @DeleteMapping("/delete-post-profile")
    public ResponseEntity<Void> deletePostProfile(@RequestBody EditContent request, Authentication authentication) {
        postService.deletePostProfile(request,authentication.getName());
        return ResponseEntity.noContent().build();
    }

}

