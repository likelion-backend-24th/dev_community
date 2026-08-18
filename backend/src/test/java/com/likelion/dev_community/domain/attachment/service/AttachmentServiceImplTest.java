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
import com.likelion.dev_community.domain.user.entity.Role;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceImplTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private FileStorageService fileStorageService;

    private AttachmentServiceImpl attachmentService;

    @BeforeEach
    void setUp() {
        attachmentService = new AttachmentServiceImpl(attachmentRepository, questionRepository, answerRepository, fileStorageService);
    }

    @Test
    void 질문_작성자는_코드_파일을_정상적으로_업로드한다() {
        User author = createUser(1L, "author");
        Question question = createQuestion(10L, author);
        MultipartFile file = createFile("Hello.java", "text/x-java", 100);

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(attachmentRepository.countByTargetTypeAndTargetId(AttachmentTargetType.QUESTION, 10L)).thenReturn(0L);
        when(fileStorageService.store(any(), anyString())).thenReturn("stored-uuid.java");
        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(inv -> inv.getArgument(0));

        List<AttachmentResponse> responses = attachmentService.upload(AttachmentTargetType.QUESTION, 10L, List.of(file), 1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getOriginalFilename()).isEqualTo("Hello.java");
        verify(fileStorageService).store(file, "java");
    }

    @Test
    void 답변_작성자는_이미지를_정상적으로_업로드한다() {
        User author = createUser(1L, "author");
        Question question = createQuestion(10L, createUser(2L, "asker"));
        Answer answer = createAnswer(100L, question, author);
        MultipartFile file = createFile("photo.png", "image/png", 1024);

        when(answerRepository.findById(100L)).thenReturn(Optional.of(answer));
        when(attachmentRepository.countByTargetTypeAndTargetId(AttachmentTargetType.ANSWER, 100L)).thenReturn(0L);
        when(fileStorageService.store(any(), anyString())).thenReturn("stored-uuid.png");
        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(inv -> inv.getArgument(0));

        List<AttachmentResponse> responses = attachmentService.upload(AttachmentTargetType.ANSWER, 100L, List.of(file), 1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getContentType()).isEqualTo("image/png");
        verify(fileStorageService).store(file, "png");
    }

    @Test
    void 파일_용량이_2MB를_초과하면_예외가_발생한다() {
        User author = createUser(1L, "author");
        Question question = createQuestion(10L, author);
        MultipartFile file = createFile("big.png", "image/png", 3 * 1024 * 1024);

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(attachmentRepository.countByTargetTypeAndTargetId(AttachmentTargetType.QUESTION, 10L)).thenReturn(0L);

        assertThatThrownBy(() -> attachmentService.upload(AttachmentTargetType.QUESTION, 10L, List.of(file), 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.FILE_TOO_LARGE));
    }

    @Test
    void 허용되지_않는_확장자면_예외가_발생한다() {
        User author = createUser(1L, "author");
        Question question = createQuestion(10L, author);
        MultipartFile file = createFile("virus.exe", "application/octet-stream", 100);

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(attachmentRepository.countByTargetTypeAndTargetId(AttachmentTargetType.QUESTION, 10L)).thenReturn(0L);

        assertThatThrownBy(() -> attachmentService.upload(AttachmentTargetType.QUESTION, 10L, List.of(file), 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.UNSUPPORTED_FILE_TYPE));
    }

    @Test
    void 확장자가_없는_파일명이면_예외가_발생한다() {
        User author = createUser(1L, "author");
        Question question = createQuestion(10L, author);
        MultipartFile file = createFile("noextension", "application/octet-stream", 100);

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(attachmentRepository.countByTargetTypeAndTargetId(AttachmentTargetType.QUESTION, 10L)).thenReturn(0L);

        assertThatThrownBy(() -> attachmentService.upload(AttachmentTargetType.QUESTION, 10L, List.of(file), 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.UNSUPPORTED_FILE_TYPE));
    }

    @Test
    void 기존_첨부파일과_합쳐_5개를_초과하면_예외가_발생한다() {
        User author = createUser(1L, "author");
        Question question = createQuestion(10L, author);
        MultipartFile file = createFile("Hello.java", "text/x-java", 100);

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(attachmentRepository.countByTargetTypeAndTargetId(AttachmentTargetType.QUESTION, 10L)).thenReturn(5L);

        assertThatThrownBy(() -> attachmentService.upload(AttachmentTargetType.QUESTION, 10L, List.of(file), 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.ATTACHMENT_LIMIT_EXCEEDED));
    }

    @Test
    void 질문_작성자가_아니면_업로드시_예외가_발생한다() {
        User author = createUser(1L, "author");
        Question question = createQuestion(10L, author);
        MultipartFile file = createFile("Hello.java", "text/x-java", 100);

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> attachmentService.upload(AttachmentTargetType.QUESTION, 10L, List.of(file), 999L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void 답변_작성자가_아니면_업로드시_예외가_발생한다() {
        User author = createUser(1L, "author");
        Question question = createQuestion(10L, createUser(2L, "asker"));
        Answer answer = createAnswer(100L, question, author);
        MultipartFile file = createFile("Hello.java", "text/x-java", 100);

        when(answerRepository.findById(100L)).thenReturn(Optional.of(answer));

        assertThatThrownBy(() -> attachmentService.upload(AttachmentTargetType.ANSWER, 100L, List.of(file), 999L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void 존재하지_않는_질문에_업로드하면_예외가_발생한다() {
        MultipartFile file = createFile("Hello.java", "text/x-java", 100);

        when(questionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.upload(AttachmentTargetType.QUESTION, 999L, List.of(file), 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void 존재하지_않는_답변에_업로드하면_예외가_발생한다() {
        MultipartFile file = createFile("Hello.java", "text/x-java", 100);

        when(answerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.upload(AttachmentTargetType.ANSWER, 999L, List.of(file), 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void 정상적으로_첨부파일_목록을_조회한다() {
        Attachment attachment1 = createAttachment(1L, AttachmentTargetType.QUESTION, 10L, 1L, "a.java");
        Attachment attachment2 = createAttachment(2L, AttachmentTargetType.QUESTION, 10L, 1L, "b.png");

        when(attachmentRepository.findByTargetTypeAndTargetIdOrderByCreatedAtAsc(AttachmentTargetType.QUESTION, 10L))
                .thenReturn(List.of(attachment1, attachment2));

        List<AttachmentResponse> responses = attachmentService.list(AttachmentTargetType.QUESTION, 10L);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getOriginalFilename()).isEqualTo("a.java");
        assertThat(responses.get(1).getOriginalFilename()).isEqualTo("b.png");
    }

    @Test
    void 업로더_본인은_첨부파일을_삭제할_수_있다() {
        Attachment attachment = createAttachment(1L, AttachmentTargetType.QUESTION, 10L, 1L, "a.java");

        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(attachment));

        attachmentService.delete(1L, 1L, false);

        verify(fileStorageService).delete(attachment.getStoredFilename());
        verify(attachmentRepository).delete(attachment);
    }

    @Test
    void 관리자는_타인의_첨부파일도_삭제할_수_있다() {
        Attachment attachment = createAttachment(1L, AttachmentTargetType.QUESTION, 10L, 1L, "a.java");

        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(attachment));

        attachmentService.delete(1L, 999L, true);

        verify(fileStorageService).delete(attachment.getStoredFilename());
        verify(attachmentRepository).delete(attachment);
    }

    @Test
    void 업로더도_관리자도_아니면_삭제시_예외가_발생한다() {
        Attachment attachment = createAttachment(1L, AttachmentTargetType.QUESTION, 10L, 1L, "a.java");

        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(attachment));

        assertThatThrownBy(() -> attachmentService.delete(1L, 999L, false))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
        verify(attachmentRepository, org.mockito.Mockito.never()).delete(any());
    }

    @Test
    void 존재하지_않는_첨부파일_삭제시_예외가_발생한다() {
        when(attachmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.delete(999L, 1L, false))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void 정상적으로_다운로드용_첨부파일을_조회한다() {
        Attachment attachment = createAttachment(1L, AttachmentTargetType.QUESTION, 10L, 1L, "a.java");

        when(attachmentRepository.findById(1L)).thenReturn(Optional.of(attachment));

        Attachment found = attachmentService.getForDownload(1L);

        assertThat(found.getOriginalFilename()).isEqualTo("a.java");
    }

    @Test
    void 존재하지_않는_첨부파일_다운로드_조회시_예외가_발생한다() {
        when(attachmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.getForDownload(999L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    private MultipartFile createFile(String filename, String contentType, int size) {
        return new MockMultipartFile("files", filename, contentType, new byte[size]);
    }

    private User createUser(Long id, String nickname) {
        User user = User.builder()
                .username(nickname)
                .password("encoded-password")
                .nickname(nickname)
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
        setId(user, id);
        return user;
    }

    private Question createQuestion(Long id, User author) {
        Question question = Question.builder()
                .author(author)
                .title("제목")
                .content("내용")
                .build();
        setId(question, id);
        return question;
    }

    private Answer createAnswer(Long id, Question question, User author) {
        Answer answer = Answer.builder()
                .question(question)
                .author(author)
                .content("내용")
                .build();
        setId(answer, id);
        return answer;
    }

    private Attachment createAttachment(Long id, AttachmentTargetType targetType, Long targetId, Long uploaderId, String originalFilename) {
        Attachment attachment = Attachment.builder()
                .targetType(targetType)
                .targetId(targetId)
                .uploaderId(uploaderId)
                .originalFilename(originalFilename)
                .storedFilename("stored-" + id + "-" + originalFilename)
                .contentType("application/octet-stream")
                .fileSize(100)
                .build();
        setId(attachment, id);
        return attachment;
    }

    private void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
