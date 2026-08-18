package com.likelion.dev_community.domain.attachment.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.answer.entity.Answer;
import com.likelion.dev_community.domain.answer.repository.AnswerRepository;
import com.likelion.dev_community.domain.attachment.dto.AttachmentResponse;
import com.likelion.dev_community.domain.attachment.entity.Attachment;
import com.likelion.dev_community.domain.attachment.entity.AttachmentTargetType;
import com.likelion.dev_community.domain.attachment.repository.AttachmentRepository;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttachmentServiceImpl implements AttachmentService {

    private static final long MAX_FILE_SIZE = 2L * 1024 * 1024; // 2MB
    private static final int MAX_FILES_PER_TARGET = 5;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            // 코드
            "java", "js", "jsx", "ts", "tsx", "py", "go", "rb", "c", "cpp", "h", "hpp",
            "html", "css", "sql", "json", "xml", "yml", "yaml", "md", "sh", "kt", "swift", "php",
            // 이미지 (작은 사진 첨부)
            "png", "jpg", "jpeg", "gif", "webp"
    );

    private final AttachmentRepository attachmentRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public List<AttachmentResponse> upload(AttachmentTargetType targetType, Long targetId, List<MultipartFile> files, Long uploaderId) {
        validateOwnership(targetType, targetId, uploaderId);

        long existingCount = attachmentRepository.countByTargetTypeAndTargetId(targetType, targetId);
        if (existingCount + files.size() > MAX_FILES_PER_TARGET) {
            throw new CustomException(ErrorCode.ATTACHMENT_LIMIT_EXCEEDED);
        }

        return files.stream()
                .map(file -> uploadOne(targetType, targetId, file, uploaderId))
                .map(AttachmentResponse::from)
                .toList();
    }

    private Attachment uploadOne(AttachmentTargetType targetType, Long targetId, MultipartFile file, Long uploaderId) {
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "빈 파일은 첨부할 수 없습니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException(ErrorCode.FILE_TOO_LARGE);
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_FILE_TYPE);
        }

        String storedFilename = fileStorageService.store(file, extension);

        Attachment attachment = Attachment.builder()
                .targetType(targetType)
                .targetId(targetId)
                .uploaderId(uploaderId)
                .originalFilename(file.getOriginalFilename())
                .storedFilename(storedFilename)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .build();

        return attachmentRepository.save(attachment);
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new CustomException(ErrorCode.UNSUPPORTED_FILE_TYPE);
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private void validateOwnership(AttachmentTargetType targetType, Long targetId, Long uploaderId) {
        if (targetType == AttachmentTargetType.QUESTION) {
            Question question = questionRepository.findById(targetId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "요청한 질문을 찾을 수 없습니다."));
            if (!question.getAuthor().getId().equals(uploaderId)) {
                throw new CustomException(ErrorCode.FORBIDDEN);
            }
        } else {
            Answer answer = answerRepository.findById(targetId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "요청한 답변을 찾을 수 없습니다."));
            if (!answer.getAuthor().getId().equals(uploaderId)) {
                throw new CustomException(ErrorCode.FORBIDDEN);
            }
        }
    }

    @Override
    public List<AttachmentResponse> list(AttachmentTargetType targetType, Long targetId) {
        return attachmentRepository.findByTargetTypeAndTargetIdOrderByCreatedAtAsc(targetType, targetId)
                .stream()
                .map(AttachmentResponse::from)
                .toList();
    }

    @Override
    public Attachment getForDownload(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "요청한 첨부파일을 찾을 수 없습니다."));
    }

    @Override
    @Transactional
    public void delete(Long attachmentId, Long userId, boolean isAdmin) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "요청한 첨부파일을 찾을 수 없습니다."));

        if (!isAdmin && !attachment.getUploaderId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        fileStorageService.delete(attachment.getStoredFilename());
        attachmentRepository.delete(attachment);
    }
}
