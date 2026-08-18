package com.likelion.dev_community.domain.attachment.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.attachment.dto.AttachmentResponse;
import com.likelion.dev_community.domain.attachment.entity.Attachment;
import com.likelion.dev_community.domain.attachment.entity.AttachmentTargetType;
import com.likelion.dev_community.domain.attachment.service.AttachmentService;
import com.likelion.dev_community.domain.attachment.service.FileStorageService;
import com.likelion.dev_community.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final FileStorageService fileStorageService;

    @PostMapping("/api/questions/{questionId}/attachments")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> uploadQuestionAttachments(
            @PathVariable Long questionId,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<AttachmentResponse> response = attachmentService.upload(
                AttachmentTargetType.QUESTION, questionId, files, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("첨부파일 등록 완료", response));
    }

    @PostMapping("/api/answers/{answerId}/attachments")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> uploadAnswerAttachments(
            @PathVariable Long answerId,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<AttachmentResponse> response = attachmentService.upload(
                AttachmentTargetType.ANSWER, answerId, files, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("첨부파일 등록 완료", response));
    }

    @GetMapping("/api/questions/{questionId}/attachments")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getQuestionAttachments(@PathVariable Long questionId) {
        List<AttachmentResponse> response = attachmentService.list(AttachmentTargetType.QUESTION, questionId);
        return ResponseEntity.ok(ApiResponse.success("첨부파일 목록 조회 성공", response));
    }

    @GetMapping("/api/answers/{answerId}/attachments")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAnswerAttachments(@PathVariable Long answerId) {
        List<AttachmentResponse> response = attachmentService.list(AttachmentTargetType.ANSWER, answerId);
        return ResponseEntity.ok(ApiResponse.success("첨부파일 목록 조회 성공", response));
    }

    // 이미지는 <img>에서 바로 표시, 그 외 코드 파일은 다운로드되도록 Content-Disposition을 파일별로 다르게 설정
    @GetMapping("/api/attachments/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
        Attachment attachment = attachmentService.getForDownload(attachmentId);
        Resource resource = fileStorageService.loadAsResource(attachment.getStoredFilename());

        String encodedFilename = UriUtils.encode(attachment.getOriginalFilename(), StandardCharsets.UTF_8);
        String disposition = (attachment.getContentType() != null && attachment.getContentType().startsWith("image/"))
                ? "inline"
                : "attachment";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        attachment.getContentType() != null ? attachment.getContentType() : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        disposition + "; filename*=UTF-8''" + encodedFilename)
                .body(resource);
    }

    @DeleteMapping("/api/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        attachmentService.delete(attachmentId, userDetails.getId(), userDetails.isAdmin());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
