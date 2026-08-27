package com.likelion.dev_community.domain.user.service;

import com.likelion.dev_community.common.avatar.AvatarPalette;
import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.common.exception.NotFound;
import com.likelion.dev_community.domain.answer.dto.AnswerResponse;
import com.likelion.dev_community.domain.answer.entity.Answer;
import com.likelion.dev_community.domain.answer.repository.AnswerRepository;
import com.likelion.dev_community.domain.answer.repository.QuestionAnswerCount;
import com.likelion.dev_community.domain.question.dto.QuestionSummaryResponse;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.payment.service.PaymentService;
import com.likelion.dev_community.domain.question.repository.QuestionRepository;
import com.likelion.dev_community.domain.question.repository.QuestionTagRepository;
import com.likelion.dev_community.domain.subscription.service.SubscriptionService;
import com.likelion.dev_community.domain.user.dto.userDto.AvatarColorResponse;
import com.likelion.dev_community.domain.user.dto.userDto.UserInfoRequest;
import com.likelion.dev_community.domain.user.dto.userDto.UserInfoResponse;
import com.likelion.dev_community.domain.user.dto.userDto.UserPwRequest;
import com.likelion.dev_community.domain.user.entity.AuthProvider;
import com.likelion.dev_community.domain.user.entity.Role;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.entity.UserStatus;
import com.likelion.dev_community.domain.user.repository.RefreshTokenRepository;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import com.likelion.dev_community.security.jwt.CookieProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieProvider cookieProvider;
    private final QuestionRepository questionRepository;
    private final QuestionTagRepository questionTagRepository;
    private final AnswerRepository answerRepository;
    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;

    // 회원 정보 조회
    public UserInfoResponse getUserInfo(Long userId){
        User user = findUserById(userId);

        return UserInfoResponse.from(user);
    }

    // 회원 정보 수정 (닉네임)
    @Transactional
    public UserInfoResponse updateUserInfo(UserInfoRequest request, Long userId){
        User user = findUserById(userId);

        String newNickname = request.getNickname();

        if(!user.getNickname().equals(newNickname) && userRepository.existsByNickname(newNickname))
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용중인 닉네임입니다." + request.getNickname());


        user.updateUser(request.getNickname());

        return UserInfoResponse.from(user);
    }

    // 비밀번호 변경
    @Transactional
    public void updateUserPassword(UserPwRequest request, Long userId, HttpServletResponse httpServletResponse){
        User user = findUserById(userId);

        if(!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword()))
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));

        refreshTokenRepository.deleteById(userId);

        ResponseCookie cookie = cookieProvider.clearCookie("refreshToken");
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(Long userId, String currentPassword, HttpServletResponse httpServletResponse){
        User user = findUserById(userId);

        boolean isOAuthUser = user.getProvider() != AuthProvider.LOCAL;

        if (!isOAuthUser && !passwordEncoder.matches(currentPassword, user.getPassword()))
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);

        user.withdraw(); // 사용자 상태 withdrawn

        paymentService.cancelSubscriptionForWithdrawal(userId); // 회원 탈퇴 시 구독 해지 처리

        refreshTokenRepository.deleteById(userId);

        ResponseCookie cookie = cookieProvider.clearCookie("refreshToken");
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // 모든 유저 조회 (관리자) - expertRequested=true면 전문가 등급 요청한 유저만 조회
    @Transactional(readOnly = true)
    public Page<UserInfoResponse> getAllUsersInfo(Pageable pageable, Boolean expertRequested){
        Page<User> allUsers = Boolean.TRUE.equals(expertRequested)
                ? userRepository.findAllByExpertRequestedTrue(pageable)
                : userRepository.findAll(pageable);

        return allUsers.map(UserInfoResponse::from);
    }

    // 유저 정지
    @Transactional
    public UserInfoResponse userSuspension(Long userId){
        User user = findUserById(userId);

        if(user.getRole() == Role.ADMIN)
            throw new CustomException(ErrorCode.FORBIDDEN, "관리자 계정은 정지할 수 없습니다.");

        if(user.getStatus().equals(UserStatus.SUSPENDED))
            throw new CustomException(ErrorCode.ALREADY_SUSPENDED_USER);

        user.suspend();
        user.cancelExpertRequest(); // 정지된 유저의 전문가 등급 요청은 자동 거절 처리

        paymentService.revokeNextChargeForSuspension(userId); // 구독 중인 유저가 정지당하면 다음 회차 구독을 취소시킴

        refreshTokenRepository.deleteById(userId);

        return UserInfoResponse.from(user);
    }


    // 유저 정지 해제
    @Transactional
    public UserInfoResponse userUnsuspension(Long userId){
        User user = findUserById(userId);

        if(!user.getStatus().equals(UserStatus.SUSPENDED))
            throw new CustomException(ErrorCode.NOT_SUSPENDED_USER);

        user.unsuspend();

        return UserInfoResponse.from(user);
    }

    // 전문가 지정 (관리자)
    @Transactional
    public UserInfoResponse grantExpert(Long userId){
        User user = findUserById(userId);

        if(user.isExpert())
            throw new CustomException(ErrorCode.ALREADY_EXPERT);

        user.grantExpert();

        return UserInfoResponse.from(user);
    }

    // 전문가 지정 해제 (관리자)
    @Transactional
    public UserInfoResponse revokeExpert(Long userId){
        User user = findUserById(userId);

        user.revokeExpert();

        return UserInfoResponse.from(user);
    }

    // 전문가 등급 신청 (본인)
    @Transactional
    public UserInfoResponse requestExpert(Long userId){
        User user = findUserById(userId);

        if(user.isExpert())
            throw new CustomException(ErrorCode.ALREADY_EXPERT);

        if(user.isExpertRequested())
            throw new CustomException(ErrorCode.ALREADY_REQUESTED_EXPERT);

        user.requestExpert();

        return UserInfoResponse.from(user);
    }

    // 전문가 등급 신청 거절 (관리자)
    @Transactional
    public UserInfoResponse rejectExpertRequest(Long userId){
        User user = findUserById(userId);

        if(!user.isExpertRequested())
            throw new CustomException(ErrorCode.NOT_REQUESTED_EXPERT);

        user.cancelExpertRequest();

        return UserInfoResponse.from(user);
    }

    // 멤버십 아바타 색상 리롤. 결제 갱신(구독 startedAt)마다 1회로 제한한다 —
    // 아직 한 번도 안 뽑았거나, 마지막으로 뽑은 시점이 현재 결제 주기 시작보다 이전이면 허용.
    @Transactional
    public AvatarColorResponse rerollAvatarColor(Long userId, boolean isAdmin){
        subscriptionService.requireActiveSubscriber(userId, isAdmin);

        User user = findUserById(userId);

        Optional<LocalDateTime> cycleStartedAt = subscriptionService.getActiveSubscriptionStartedAt(userId);
        LocalDateTime rolledAt = user.getAvatarColorRolledAt();

        boolean alreadyRolledThisCycle = rolledAt != null
                && cycleStartedAt.isPresent()
                && !rolledAt.isBefore(cycleStartedAt.get());

        if (alreadyRolledThisCycle) {
            throw new CustomException(ErrorCode.AVATAR_COLOR_ALREADY_ROLLED);
        }

        LocalDateTime now = LocalDateTime.now();
        user.rollAvatarColor(AvatarPalette.rollRandom(), now);

        return AvatarColorResponse.of(user.getAvatarColorHex(), now);
    }

    public User findUserById(Long userId){
        return userRepository.findById(userId).orElseThrow(NotFound.USER::exception);
    }

    // 내 질문 목록 조회
    @Transactional(readOnly = true)
    public Page<QuestionSummaryResponse> getMyQuestions(Long userId, Pageable pageable){
        Page<Question> myQuestions = questionRepository.findAllByAuthorIdOrderByCreatedAtDesc(userId, pageable);

        if (myQuestions.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> questionIds = myQuestions.getContent().stream()
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

        return myQuestions.map(question -> QuestionSummaryResponse.of(
                question,
                Math.toIntExact(answerCountMap.getOrDefault(question.getId(),0L)),
                tagMap.getOrDefault(question.getId(), Collections.emptyList())
        ));
    }

    // 내 답변 목록 조회
    @Transactional(readOnly = true)
    public Page<AnswerResponse> getMyAnswers(Long userId, Pageable pageable){
        Page<Answer> myAnswers = answerRepository.findAllByAuthorIdOrderByCreatedAtDesc(userId, pageable);

        return myAnswers.map(AnswerResponse::from);
    }
}