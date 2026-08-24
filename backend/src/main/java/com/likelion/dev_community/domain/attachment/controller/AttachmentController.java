package com.likelion.dev_community.domain.attachment.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.attachment.dto.AttachmentResponse;
import com.likelion.dev_community.domain.attachment.entity.Attachment;
import com.likelion.dev_community.domain.attachment.entity.AttachmentTargetType;
import com.likelion.dev_community.domain.attachment.service.AttachmentService;
import com.likelion.dev_community.domain.attachment.service.FileStorageService;
import com.likelion.dev_community.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "첨부파일", description = "질문/답변에 파일을 업로드·조회·다운로드·삭제하는 API. 로컬 디스크에 저장되며 최대 2MB, 5개까지 허용")
@RestController
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final FileStorageService fileStorageService;

    @Operation(summary = "질문 첨부파일 업로드", description = "특정 질문에 파일을 첨부. 허용 확장자·용량 초과 시 400. 로그인 필요.")
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

    @Operation(summary = "답변 첨부파일 업로드", description = "특정 답변에 파일을 첨부. 허용 확장자·용량 초과 시 400. 로그인 필요.")
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

    @Operation(summary = "질문 첨부파일 목록 조회", description = "특정 질문에 첨부된 파일 목록을 조회. 인증 불필요.")
    @GetMapping("/api/questions/{questionId}/attachments")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getQuestionAttachments(@PathVariable Long questionId) {
        List<AttachmentResponse> response = attachmentService.list(AttachmentTargetType.QUESTION, questionId);
        return ResponseEntity.ok(ApiResponse.success("첨부파일 목록 조회 성공", response));
    }

    @Operation(summary = "답변 첨부파일 목록 조회", description = "특정 답변에 첨부된 파일 목록을 조회. 인증 불필요.")
    @GetMapping("/api/answers/{answerId}/attachments")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAnswerAttachments(@PathVariable Long answerId) {
        List<AttachmentResponse> response = attachmentService.list(AttachmentTargetType.ANSWER, answerId);
        return ResponseEntity.ok(ApiResponse.success("첨부파일 목록 조회 성공", response));
    }

    // 이미지는 <img>에서 바로 표시, 그 외 코드 파일은 다운로드되도록 Content-Disposition을 파일별로 다르게 설정
    @Operation(summary = "첨부파일 다운로드", description = "첨부파일을 다운로드. 이미지는 브라우저에서 바로 표시(inline)되고, 그 외 파일은 다운로드(attachment)됨. 인증 불필요.")
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

    @Operation(summary = "첨부파일 삭제", description = "본인이 업로드한 첨부파일을 삭제. ADMIN은 타인 첨부파일도 삭제 가능.")
    @DeleteMapping("/api/attachments/{attachmentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        attachmentService.delete(attachmentId, userDetails.getId(), userDetails.isAdmin());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
