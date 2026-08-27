package com.likelion.dev_community.domain.report.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.common.exception.NotFound;
import com.likelion.dev_community.domain.answer.entity.Answer;
import com.likelion.dev_community.domain.answer.repository.AnswerRepository;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.repository.QuestionRepository;
import com.likelion.dev_community.domain.report.dto.ReportProcessRequest;
import com.likelion.dev_community.domain.report.dto.ReportRequest;
import com.likelion.dev_community.domain.report.dto.ReportResponse;
import com.likelion.dev_community.domain.report.entity.Report;
import com.likelion.dev_community.domain.report.entity.ReportStatus;
import com.likelion.dev_community.domain.report.entity.ReportTargetType;
import com.likelion.dev_community.domain.report.repository.ReportRepository;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    // 신고 접수
    @Transactional
    public ReportResponse report(Long userId, ReportRequest reportRequest){
        User user = userRepository.findById(userId).orElseThrow(NotFound.USER::exception);

        Long targetUserId;
        String targetUserNickname;

        if(reportRequest.getTargetType() == ReportTargetType.QUESTION){
            Question question = questionRepository.findById(reportRequest.getTargetId()).orElseThrow(NotFound.QUESTION::exception);

            if(question.getAuthor().getId().equals(user.getId())){
                throw new CustomException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
            }

            targetUserId = question.getAuthor().getId();
            targetUserNickname = question.getAuthor().getNickname();
        } else {
            Answer answer = answerRepository.findById(reportRequest.getTargetId()).orElseThrow(NotFound.ANSWER::exception);

            if(answer.getAuthor().getId().equals(user.getId())){
                throw new CustomException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
            }

            targetUserId = answer.getAuthor().getId();
            targetUserNickname = answer.getAuthor().getNickname();
        }

        Report report = reportRepository.save(Report.builder()
                .reporter(user)
                .targetType(reportRequest.getTargetType())
                .targetId(reportRequest.getTargetId())
                .targetUserId(targetUserId)
                .reason(reportRequest.getReason())
                .build());

        return ReportResponse.from(report, targetUserNickname);
    }


    // 신고 목록 조회
    @Transactional(readOnly = true)
    public Page<ReportResponse> getReports(ReportStatus status, Pageable pageable){

        Page<Report> reports;

        if(status == null) {
            reports = reportRepository.findAll(pageable);
        }
        else{
            reports = reportRepository.findByStatus(status, pageable);
        }

        List<Long> userIds = reports.stream()
                .map(Report::getTargetUserId)
                .distinct()
                .toList();

        List<User> users = userRepository.findAllById(userIds);

        Map<Long, String> userIdAndNicknameMap = users.stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        return reports.map(report -> ReportResponse.from(report, userIdAndNicknameMap.get(report.getTargetUserId())));
    }

    // 신고 처리
    @Transactional
    public ReportResponse processReport(Long reportId, ReportProcessRequest request){
        Report report = reportRepository.findById(reportId).orElseThrow(NotFound.REPORT::exception);

        ReportStatus newStatus = request.getStatus();

        if(newStatus != ReportStatus.RESOLVED && newStatus != ReportStatus.REJECTED){
            throw new CustomException(ErrorCode.INVALID_INPUT, "처리 상태는 RESOLVED 또는 REJECTED만 가능합니다.");
        }
        if(report.getStatus() != ReportStatus.PENDING){
            throw new CustomException(ErrorCode.ALREADY_PROCESSED_REPORT);
        }

        switch(newStatus){
            case REJECTED -> report.reject();
            case RESOLVED -> report.resolve();
        }

        User user = userRepository.findById(report.getTargetUserId()).orElseThrow(NotFound.USER::exception);

        return ReportResponse.from(report, user.getNickname());
    }

    // 유저 신고 누적 카운트
    @Transactional(readOnly = true)
    public Long countByTargetUserId(Long targetUserId){

        return reportRepository.countByTargetUserIdAndStatus(targetUserId, ReportStatus.RESOLVED);
    }
}
