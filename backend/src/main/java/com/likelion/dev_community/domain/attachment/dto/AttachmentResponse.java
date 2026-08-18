package com.likelion.dev_community.domain.attachment.dto;

import com.likelion.dev_community.domain.attachment.entity.Attachment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AttachmentResponse {

    private final Long id;
    private final String originalFilename;
    private final String contentType;
    private final long fileSize;
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
