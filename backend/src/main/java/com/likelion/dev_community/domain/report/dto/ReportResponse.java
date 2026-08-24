package com.likelion.dev_community.domain.report.dto;

import com.likelion.dev_community.domain.report.entity.Report;
import com.likelion.dev_community.domain.report.entity.ReportStatus;
import com.likelion.dev_community.domain.report.entity.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ReportResponse {

    @Schema(example = "1")
    private final Long id;

    @Schema(example = "5")
    private final Long reporterId;

    @Schema(example = "질문왕")
    private final String reporterNickname;

    private final ReportTargetType targetType;

    @Schema(example = "10")
    private final Long targetId;

    @Schema(example = "3")
    private final Long targetUserId;

    @Schema(example = "BE24-Team4")
    private final String targetUserNickname;

    @Schema(example = "부적절한 내용이 포함되어 있습니다.")
    private final String reason;

    private final ReportStatus status;

    @Schema(example = "2026-08-23T10:00:00")
    private final LocalDateTime createdAt;

    public static ReportResponse from(Report report, String targetUserNickname){
        return new ReportResponse(
                report.getId(),
                report.getReporter().getId(),
                report.getReporter().getNickname(),
                report.getTargetType(),
                report.getTargetId(),
                report.getTargetUserId(),
                targetUserNickname,
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt()
        );
    }
}
