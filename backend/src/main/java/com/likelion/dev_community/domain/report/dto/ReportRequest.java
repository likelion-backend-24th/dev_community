package com.likelion.dev_community.domain.report.dto;

import com.likelion.dev_community.domain.report.entity.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportRequest {

    @NotNull
    private final ReportTargetType targetType;

    @NotNull
    @Schema(example = "10", description = "신고 대상의 ID. targetType에 따라 질문 ID 또는 답변 ID")
    private final Long targetId;

    @NotBlank
    @Schema(example = "부적절한 내용이 포함되어 있습니다.", description = "신고 사유")
    private final String reason;
}
