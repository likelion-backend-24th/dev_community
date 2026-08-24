package com.likelion.dev_community.domain.attachment.dto;

import com.likelion.dev_community.domain.attachment.entity.Attachment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AttachmentResponse {

    @Schema(example = "1")
    private final Long id;
    @Schema(example = "screenshot.png")
    private final String originalFilename;
    @Schema(example = "image/png")
    private final String contentType;
    @Schema(example = "204800")
    private final long fileSize;
    @Schema(example = "2026-08-23T10:00:00")
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
