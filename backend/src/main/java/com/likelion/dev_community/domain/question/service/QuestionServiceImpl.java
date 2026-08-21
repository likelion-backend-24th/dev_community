package com.likelion.dev_community.domain.question.service;

import com.likelion.dev_community.common.AuthorizationValidator;
import com.likelion.dev_community.common.ai.GeminiClient;
import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.common.viewcount.ViewCountService;
import com.likelion.dev_community.common.xss.XssSanitizer;
import com.likelion.dev_community.domain.answer.entity.Answer;
import com.likelion.dev_community.domain.answer.repository.AnswerRepository;
import com.likelion.dev_community.domain.answer.repository.QuestionAnswerCount;
import com.likelion.dev_community.domain.chat.entity.ChatRoom;
import com.likelion.dev_community.domain.chat.repository.ChatRoomRepository;
import com.likelion.dev_community.domain.question.dto.*;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionSortType;
import com.likelion.dev_community.domain.question.entity.QuestionStatus;
import com.likelion.dev_community.domain.question.entity.QuestionType;
import com.likelion.dev_community.domain.question.entity.Tag;
import com.likelion.dev_community.domain.question.entity.QuestionTag;
import com.likelion.dev_community.domain.question.repository.CodeCommentRepository;
import com.likelion.dev_community.domain.question.repository.QuestionRepository;
import com.likelion.dev_community.domain.question.repository.QuestionTagRepository;
import com.likelion.dev_community.domain.question.repository.TagRepository;
import com.likelion.dev_community.domain.subscription.service.SubscriptionService;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionTagRepository questionTagRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final XssSanitizer xssSanitizer;
    private final ViewCountService viewCountService;
    private final TagRepository tagRepository;
    private final SubscriptionService subscriptionService;
    private final ChatRoomRepository chatRoomRepository;
    private final CodeCommentRepository codeCommentRepository;
    private final GeminiClient geminiClient;
    private static final int MAX_TAG_COUNT = 5;
    private static final int MAX_TAG_LENGTH = 30;

    // F-06
    @Override
    public QuestionResponse createQuestion(Long userId, boolean isAdmin, QuestionCreateRequest request) {

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "찾을 수 없는 사용자 정보"));

        if (request.isPremium() || request.isAnonymous()) {
            subscriptionService.requireActiveSubscriber(userId, isAdmin);
        }

        QuestionType type = request.isPremium() ? QuestionType.from(request.getType()) : QuestionType.GENERAL;

        // 추후 마크다운 렌더링 도입 시 출력 단계(HTML 변환 후)에서 sanitize 적용 예정
        // String title = xssSanitizer.sanitize(request.getTitle());
        // String content = xssSanitizer.sanitize(request.getContent());
        String title = request.getTitle();
        String content = request.getContent();

        Question question = Question.builder()
                .author(author)
                .title(title)
                .content(content)
                .isPremium(request.isPremium())
                .isAnonymous(request.isAnonymous())
                .type(type)
                .build();

        questionRepository.save(question);

        List<String> tagNames = attachTags(question, request.getTags());

        return QuestionResponse.from(question, tagNames);
    }

    // F-07
    @Override
    @Transactional(readOnly = true)
    public Page<QuestionSummaryResponse> readQuestions(int page, int size, String sort, String keyword, String tag, String status
    ) {
        return searchQuestionSummaries(page, size, sort, keyword, tag, status, false);
    }

    // F-32
    @Override
    public Page<QuestionSummaryResponse> readPremiumQuestions(int page, int size, String sort, String keyword, String tag, String status, Long userId, boolean isAdmin
    ) {
        subscriptionService.requireActiveSubscriber(userId, isAdmin);

        return searchQuestionSummaries(page, size, sort, keyword, tag, status, true);
    }

    private Page<QuestionSummaryResponse> searchQuestionSummaries(int page, int size, String sort, String keyword, String tag, String status, boolean isPremium
    ) {
        if (page < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "page는 0 이상이어야 합니다.");
        }
        if (size < 1 || size > 100) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "size는 1~100 사이여야 합니다.");
        }

        Pageable pageable = PageRequest.of(page, size);
        QuestionSortType sortType = QuestionSortType.from(sort);

        // F-18
        Long tagId = null;
        if (tag != null && !tag.isBlank()) {
            String normalizedTag = tag.trim().toLowerCase();
            Tag foundTag = tagRepository.findByName(normalizedTag)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 태그"));
            tagId = foundTag.getId();
        }

        // F-19
        QuestionStatus questionStatus = statusFilter(status);

        String trimmedKeyword = keyword != null ? keyword.trim() : null;

        Page<Question> questions = isPremium
                ? questionRepository.searchPremiumQuestions(trimmedKeyword, tagId, questionStatus, sortType, pageable)
                : questionRepository.searchQuestions(trimmedKeyword, tagId, questionStatus, sortType, pageable);

        if (questions.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> questionIds = questions.getContent().stream()
                .map(Question::getId)
                .toList();

        Map<Long, List<String>> tagMap = questionTagRepository.findByQuestionIdInOrderBySortOrderAsc(questionIds).stream()
                .collect(Collectors.groupingBy(
                        qt -> qt.getQuestion().getId(),
                        Collectors.mapping(qt -> qt.getTag().getName(), Collectors.toList())
                ));

        Map<Long, Long> answerCountMap = answerRepository.countByQuestionIdIn(questionIds).stream()
                .collect(Collectors.toMap(
                        QuestionAnswerCount::getQuestionId,
                        QuestionAnswerCount::getCount
                ));

        return questions.map(question -> QuestionSummaryResponse.of(
                question,
                Math.toIntExact(answerCountMap.getOrDefault(question.getId(), 0L)),
                tagMap.getOrDefault(question.getId(), Collections.emptyList())
        ));
    }

    // F-19
    private QuestionStatus statusFilter(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return switch (status.toUpperCase()) {
            case "RESOLVED" -> QuestionStatus.RESOLVED;
            case "UNRESOLVED" -> QuestionStatus.OPEN;
            default -> throw new CustomException(ErrorCode.INVALID_INPUT, "RESOLVED 또는 UNRESOLVED만 가능");
        };
    }

    // F-08
    @Override
    public QuestionDetailResponse readDetailQuestion(Long questionId, String viewerKey, Long userId, boolean isAdmin) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "찾을 수 없는 질문"));

        if (question.isPremium()) {
            subscriptionService.requireActiveSubscriber(userId, isAdmin);
        }

        if (viewCountService.shouldIncrease(questionId, viewerKey)) {
            question.increaseViewCount();
        }

        List<String> tags = questionTagRepository.findByQuestionIdInOrderBySortOrderAsc(List.of(questionId)).stream()
                .map(qt -> qt.getTag().getName())
                .toList();

        // 커리어상담 글에서 (글 작성자가 아닌) 로그인 사용자가 이미 채팅을 열어둔 상태라면
        // 프론트가 "채팅으로 이동" 버튼을 보여줄 수 있도록 방 id를 함께 내려준다.
        Long myChatRoomId = null;
        if (userId != null && question.getType() == QuestionType.CAREER_CONSULT
                && !question.getAuthor().getId().equals(userId)) {
            myChatRoomId = chatRoomRepository.findByQuestionIdAndAnswererId(questionId, userId)
                    .map(ChatRoom::getId)
                    .orElse(null);
        }

        return QuestionDetailResponse.of(question, tags, myChatRoomId);
    }

    // F-09 (수정)
    @Override
    public QuestionResponse updateQuestion(Long questionId, Long userId, boolean isAdmin, QuestionUpdateRequest request) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "찾을 수 없는 질문"));

        checkUpdate(question, userId, isAdmin);

        if (request.isAnonymous()) {
            subscriptionService.requireActiveSubscriber(userId, isAdmin);
        }

        QuestionType type = question.isPremium() ? QuestionType.from(request.getType()) : QuestionType.GENERAL;

        if (question.getType() == QuestionType.CODE_REVIEW
                && !question.getContent().equals(request.getContent())
                && codeCommentRepository.existsByQuestionId(questionId)) {

            throw new CustomException(ErrorCode.QUESTION_CONTENT_LOCKED);
        }

        // String title = xssSanitizer.sanitize(request.getTitle());
        // String content = xssSanitizer.sanitize(request.getContent());
        question.update(request.getTitle(), request.getContent(), request.isAnonymous(), type);

        List<String> tagNames = syncTags(question, request.getTags());

        return QuestionResponse.from(question, tagNames);
    }

    // F-09 (삭제)
    @Override
    public void deleteQuestion(Long questionId, Long userId, boolean isAdmin) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "찾을 수 없는 질문"));

        checkDelete(question, userId, isAdmin);

        answerRepository.findByQuestionIdOrderByCreatedAtAsc(questionId)
                .forEach(Answer::cascadeSoftDelete);

        question.softDelete();
    }

    private static final int AI_SUMMARY_MIN_LENGTH = 300;

    // 질문 글 AI 요약(멤버십 구독자 전용). 최초 요청 시에만 Gemini를 호출하고 이후로는 캐시된 결과를 반환한다.
    @Override
    public String getSummary(Long questionId, Long userId, boolean isAdmin) {

        subscriptionService.requireActiveSubscriber(userId, isAdmin);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "찾을 수 없는 질문"));

        if (question.getSummary() != null) {
            return question.getSummary();
        }

        if (question.getContent().length() < AI_SUMMARY_MIN_LENGTH) {
            throw new CustomException(ErrorCode.AI_SUMMARY_TOO_SHORT);
        }

        String summary = geminiClient.summarize(question.getTitle(), question.getContent());
        question.updateSummary(summary);

        return summary;
    }

    private static final int AI_TAG_SUGGESTION_MIN_LENGTH = 20;

    // 질문 작성 중 본문 기준 AI 태그 추천(멤버십 구독자 전용). 작성 중인 임시 내용이라 캐싱하지 않고 매 요청마다 새로 호출한다.
    @Override
    public List<String> suggestTags(Long userId, boolean isAdmin, String title, String content) {

        subscriptionService.requireActiveSubscriber(userId, isAdmin);

        if (content.trim().length() < AI_TAG_SUGGESTION_MIN_LENGTH) {
            throw new CustomException(ErrorCode.AI_TAG_SUGGESTION_TOO_SHORT);
        }

        List<String> suggested = geminiClient.suggestTags(title, content);

        return suggested.stream()
                .map(String::trim)
                .filter(tag -> !tag.isBlank() && tag.length() <= MAX_TAG_LENGTH)
                .distinct()
                .limit(MAX_TAG_COUNT)
                .toList();
    }

    private void checkUpdate(Question question, Long userId, boolean isAdmin) {
        AuthorizationValidator.validateAuthorOrAdmin(question.getAuthor().getId(), userId, isAdmin, "본인이 작성한 질문만 수정 가능");
    }

    private void checkDelete(Question question, Long userId, boolean isAdmin) {
        AuthorizationValidator.validateAuthorOrAdmin(question.getAuthor().getId(), userId, isAdmin, "본인이 작성한 질문만 삭제 가능");
    }

    // F-10
    private List<String> attachTags(Question question, List<String> tags) {
        Set<String> normalizedNames = normalizeAndValidateTagNames(tags);

        List<String> result = new ArrayList<>();
        int order = 0;
        for (String name : normalizedNames) {
            Tag tag = resolveTag(name);
            question.createTag(tag, order++);
            result.add(tag.getName());
        }

        return result;
    }

    // F-09 (수정) - 기존에 없던 태그만 추가하고 더 이상 없는 태그는 제거하되,
    // 남아있는 태그도 sortOrder를 다시 매겨서 요청한 순서가 항상 보존되게 한다.
    private List<String> syncTags(Question question, List<String> tags) {
        Set<String> normalizedNames = normalizeAndValidateTagNames(tags);

        Map<String, QuestionTag> existingByName = question.getQuestionTags().stream()
                .collect(Collectors.toMap(qt -> qt.getTag().getName(), qt -> qt));

        question.getQuestionTags().removeIf(qt -> !normalizedNames.contains(qt.getTag().getName()));

        List<String> result = new ArrayList<>();
        int order = 0;
        for (String name : normalizedNames) {
            QuestionTag existing = existingByName.get(name);
            if (existing != null) {
                existing.changeOrder(order);
            } else {
                Tag tag = resolveTag(name);
                question.createTag(tag, order);
            }
            result.add(name);
            order++;
        }

        return result;
    }

    // 동시에 같은 신규 태그명을 저장하려는 요청이 겹치면 unique 제약 위반이 날 수 있어
    // save 실패 시 한 번 더 조회해서 재사용한다.
    private Tag resolveTag(String name) {
        return tagRepository.findByName(name)
                .orElseGet(() -> {
                    try {
                        return tagRepository.save(Tag.builder().name(name).build());
                    } catch (DataIntegrityViolationException e) {
                        return tagRepository.findByName(name).orElseThrow(() -> e);
                    }
                });
    }

    private Set<String> normalizeAndValidateTagNames(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptySet();
        }

        if (tags.size() > MAX_TAG_COUNT) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "태그는 최대 5개까지 등록 가능");
        }

        Set<String> normalizedNames = tags.stream()
                .map(this::normalizeTagName)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (String name : normalizedNames) {
            if (name.length() > MAX_TAG_LENGTH) {
                throw new CustomException(ErrorCode.INVALID_INPUT, "태그의 최대 길이는 30자");
            }
        }

        return normalizedNames;
    }

    // trim + 소문자 정규화
    private String normalizeTagName(String rawName) {
        if (rawName == null) {
            return "";
        }
        return rawName.trim().toLowerCase();
    }
}