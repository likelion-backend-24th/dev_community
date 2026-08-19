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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Deprecated
    @PostMapping("/prepare")
    public ResponseEntity<ApiResponse<PaymentPrepareResponse>> prepare(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                       @Valid @RequestBody PaymentPrepareRequest paymentPrepareRequest){
        PaymentPrepareResponse paymentPrepareResponse = paymentService.preparePayment(customUserDetails.getId(), paymentPrepareRequest.getPlanType());

        return ResponseEntity.ok(ApiResponse.success("결제 준비 완료", paymentPrepareResponse));
    }

    @Deprecated
    @PostMapping("/{paymentId}/complete")
    public ResponseEntity<ApiResponse<PaymentCompleteResponse>> complete(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                          @PathVariable String paymentId){
        PaymentCompleteResponse paymentCompleteResponse = paymentService.completePayment(paymentId, customUserDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("결제 완료 확인", paymentCompleteResponse));
    }

    @PostMapping("/billing/prepare")
    public ResponseEntity<ApiResponse<BillingKeyPrepareResponse>> prepareBillingKey(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        BillingKeyPrepareResponse response = paymentService.prepareBillingKeyIssue(customUserDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("빌링키 발급 준비 완료", response));
    }

    @PostMapping("/billing/issue")
    public ResponseEntity<ApiResponse<PaymentCompleteResponse>> issueBillingKey(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                                 @Valid @RequestBody BillingKeyIssueRequest billingKeyIssueRequest) {
        PaymentCompleteResponse response = paymentService.issueBillingKeyAndCharge(
                billingKeyIssueRequest.getBillingKey(), customUserDetails.getId(), billingKeyIssueRequest.getPlanType()
        );

        return ResponseEntity.ok(ApiResponse.success("정기결제 등록 완료", response));
    }

    @GetMapping("/me/latest")
    public ResponseEntity<ApiResponse<PaymentCompleteResponse>> myLatestPayment(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        PaymentCompleteResponse response = paymentService.getLatestPaidPayment(customUserDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("최근 결제 조회 완료", response));
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<PaymentCancelResponse>> cancel(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                     @PathVariable String paymentId,
                                                                     @Valid @RequestBody PaymentCancelRequest paymentCancelRequest) {
        PaymentCancelResponse response = paymentService.cancelPayment(paymentId, customUserDetails.getId(), paymentCancelRequest.getReason());

        return ResponseEntity.ok(ApiResponse.success("결제 취소 완료", response));
    }
}
