package com.likelion.dev_community.domain.payment.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.payment.dto.BillingKeyIssueRequest;
import com.likelion.dev_community.domain.payment.dto.BillingKeyPrepareResponse;
import com.likelion.dev_community.domain.payment.dto.PaymentCancelRequest;
import com.likelion.dev_community.domain.payment.dto.PaymentCancelResponse;
import com.likelion.dev_community.domain.payment.dto.PaymentCompleteResponse;
import com.likelion.dev_community.domain.payment.dto.PaymentPrepareRequest;
import com.likelion.dev_community.domain.payment.dto.PaymentPrepareResponse;
import com.likelion.dev_community.domain.payment.service.PaymentService;
import com.likelion.dev_community.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "결제", description = "PortOne 빌링키 발급/정기결제 등록, 결제 조회·취소를 다루는 API. 단건 결제 준비/완료 검증 엔드포인트는 빌링키 방식으로 대체되어 deprecated")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 준비 (deprecated)", description = "단건 결제 준비 API. 빌링키 기반 정기결제 방식으로 대체되어 더 이상 사용되지 않음.")
    @Deprecated
    @PostMapping("/prepare")
    public ResponseEntity<ApiResponse<PaymentPrepareResponse>> prepare(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                       @Valid @RequestBody PaymentPrepareRequest paymentPrepareRequest){
        PaymentPrepareResponse paymentPrepareResponse = paymentService.preparePayment(customUserDetails.getId(), paymentPrepareRequest.getPlanType());

        return ResponseEntity.ok(ApiResponse.success("결제 준비 완료", paymentPrepareResponse));
    }

    @Operation(summary = "결제 완료 검증 (deprecated)", description = "단건 결제 완료 검증 API. 빌링키 기반 정기결제 방식으로 대체되어 더 이상 사용되지 않음.")
    @Deprecated
    @PostMapping("/{paymentId}/complete")
    public ResponseEntity<ApiResponse<PaymentCompleteResponse>> complete(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                          @PathVariable String paymentId){
        PaymentCompleteResponse paymentCompleteResponse = paymentService.completePayment(paymentId, customUserDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("결제 완료 확인", paymentCompleteResponse));
    }

    @Operation(summary = "빌링키 발급 준비", description = "PortOne 빌링키 발급창 호출에 필요한 storeId/channelKey/issueId를 발급받음.")
    @PostMapping("/billing/prepare")
    public ResponseEntity<ApiResponse<BillingKeyPrepareResponse>> prepareBillingKey(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        BillingKeyPrepareResponse response = paymentService.prepareBillingKeyIssue(customUserDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("빌링키 발급 준비 완료", response));
    }

    @Operation(summary = "빌링키 발급 및 정기결제 등록", description = "발급된 빌링키를 검증하고 1회차 결제를 즉시 실행한 뒤, 구독 만료일에 다음 회차 결제를 PortOne에 예약.")
    @PostMapping("/billing/issue")
    public ResponseEntity<ApiResponse<PaymentCompleteResponse>> issueBillingKey(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                                 @Valid @RequestBody BillingKeyIssueRequest billingKeyIssueRequest) {
        PaymentCompleteResponse response = paymentService.issueBillingKeyAndCharge(
                billingKeyIssueRequest.getBillingKey(), customUserDetails.getId(), billingKeyIssueRequest.getPlanType()
        );

        return ResponseEntity.ok(ApiResponse.success("정기결제 등록 완료", response));
    }

    @Operation(summary = "내 최근 결제 조회", description = "본인의 가장 최근 결제완료(PAID) 건을 조회.")
    @GetMapping("/me/latest")
    public ResponseEntity<ApiResponse<PaymentCompleteResponse>> myLatestPayment(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        PaymentCompleteResponse response = paymentService.getLatestPaidPayment(customUserDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("최근 결제 조회 완료", response));
    }

    @Operation(summary = "결제 취소", description = "본인의 결제완료 건을 결제일로부터 일정 기간 이내에 환불 취소. 취소 시 구독도 즉시 해지되고 다음 회차 예약도 함께 취소됨.")
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<PaymentCancelResponse>> cancel(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                     @PathVariable String paymentId,
                                                                     @Valid @RequestBody PaymentCancelRequest paymentCancelRequest) {
        PaymentCancelResponse response = paymentService.cancelPayment(paymentId, customUserDetails.getId(), paymentCancelRequest.getReason());

        return ResponseEntity.ok(ApiResponse.success("결제 취소 완료", response));
    }
}
