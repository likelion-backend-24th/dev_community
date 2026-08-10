package com.likelion.dev_community.domain.question.service;

import com.likelion.dev_community.common.viewcount.ViewCountService;
import com.likelion.dev_community.common.xss.XssSanitizer;
import com.likelion.dev_community.domain.answer.repository.AnswerRepository;
import com.likelion.dev_community.domain.question.dto.QuestionSummaryResponse;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionSortType;
import com.likelion.dev_community.domain.question.entity.Tag;
import com.likelion.dev_community.domain.question.repository.QuestionRepository;
import com.likelion.dev_community.domain.question.repository.QuestionTagRepository;
import com.likelion.dev_community.domain.question.repository.TagRepository;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionTagRepository questionTagRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ViewCountService viewCountService;

    @Mock
    private TagRepository tagRepository;

    private QuestionServiceImpl questionService;

    @BeforeEach
    void setUp() {
        questionService = new QuestionServiceImpl(
                questionRepository, questionTagRepository, answerRepository, userRepository,
                new XssSanitizer(), viewCountService, tagRepository
        );
    }

    @Test
    void 존재하는_태그로_검색하면_해당_태그ID로_조회한다() {
        Tag tag = Tag.builder().name("java").build();
        when(tagRepository.findByName("java")).thenReturn(Optional.of(tag));
        when(questionRepository.searchQuestions(isNull(), any(), isNull(), eq(QuestionSortType.LATEST), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.<Question>of()));

        Page<QuestionSummaryResponse> result = questionService.readQuestions(
                0, 10, null, null, "JAVA", null
        );

        assertThat(result.getContent()).isEmpty();
        verify(questionRepository).searchQuestions(isNull(), any(), isNull(), eq(QuestionSortType.LATEST), any(Pageable.class));
    }
}
