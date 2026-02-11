package com.example.mangxahoi.Controller;

import com.example.mangxahoi.DTO.Request.ShareRequest;
import com.example.mangxahoi.DTO.Request.UpdateCaptionShare;
import com.example.mangxahoi.DTO.Response.ShareResponse;
import com.example.mangxahoi.Service.ShareService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/share")
@AllArgsConstructor
public class ShareController {
    private final ShareService shareService;
    @PostMapping
    public ResponseEntity<Void> shareTarget(
            @RequestBody ShareRequest shareRequest,
            Authentication authentication
            ){
        shareService.shareTarget(shareRequest,authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PatchMapping("/{shareId}")
    public ResponseEntity<ShareResponse> updateContent(
            @PathVariable Long shareId,
            @RequestBody ShareRequest request,
            Authentication authentication
            ){
        return ResponseEntity.ok(shareService.editShareTarget(shareId,request,authentication.getName()));
    }

    @DeleteMapping("/{shareId}")
    public ResponseEntity<Void> deleteShare(
            @PathVariable Long shareId,
            Authentication authentication
    ){
        shareService.deleteShareTarget(shareId,authentication.getName());
        return ResponseEntity.noContent().build();
    }

}
