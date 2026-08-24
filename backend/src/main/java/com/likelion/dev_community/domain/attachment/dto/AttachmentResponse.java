package com.likelion.dev_community.domain.attachment.dto;

import com.likelion.dev_community.domain.attachment.entity.Attachment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AttachmentResponse {

    @Schema(example = "1", description = "첨부파일 ID")
    private final Long id;
    @Schema(example = "screenshot.png", description = "업로드 당시 원본 파일명")
    private final String originalFilename;
    @Schema(example = "image/png", description = "MIME 타입")
    private final String contentType;
    @Schema(example = "204800", description = "파일 크기(byte)")
    private final long fileSize;
    @Schema(example = "2026-08-23T10:00:00", description = "업로드 일시")
    private final LocalDateTime createdAt;

    public static AttachmentResponse from(Attachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getCreatedAt()
        );
    }
}
