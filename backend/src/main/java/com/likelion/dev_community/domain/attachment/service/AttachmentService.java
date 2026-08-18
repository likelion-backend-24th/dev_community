package com.likelion.dev_community.domain.attachment.service;

import com.likelion.dev_community.domain.attachment.dto.AttachmentResponse;
import com.likelion.dev_community.domain.attachment.entity.Attachment;
import com.likelion.dev_community.domain.attachment.entity.AttachmentTargetType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {

    List<AttachmentResponse> upload(AttachmentTargetType targetType, Long targetId, List<MultipartFile> files, Long uploaderId);

    List<AttachmentResponse> list(AttachmentTargetType targetType, Long targetId);

    Attachment getForDownload(Long attachmentId);

    void delete(Long attachmentId, Long userId, boolean isAdmin);
}
