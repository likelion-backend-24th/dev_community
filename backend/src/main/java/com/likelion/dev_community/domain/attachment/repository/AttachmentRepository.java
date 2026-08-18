package com.likelion.dev_community.domain.attachment.repository;

import com.likelion.dev_community.domain.attachment.entity.Attachment;
import com.likelion.dev_community.domain.attachment.entity.AttachmentTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByTargetTypeAndTargetIdOrderByCreatedAtAsc(AttachmentTargetType targetType, Long targetId);

    long countByTargetTypeAndTargetId(AttachmentTargetType targetType, Long targetId);
}
