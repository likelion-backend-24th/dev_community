package com.likelion.dev_community.domain.payment.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.payment.dto.PaymentCompleteResponse;
import com.likelion.dev_community.domain.payment.dto.PaymentPrepareRequest;
import com.likelion.dev_community.domain.payment.dto.PaymentPrepareResponse;
import com.likelion.dev_community.domain.payment.service.PaymentService;
import com.likelion.dev_community.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping("/prepare")
    public ResponseEntity<ApiResponse<PaymentPrepareResponse>> prepare(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                       @Valid @RequestBody PaymentPrepareRequest paymentPrepareRequest){
        PaymentPrepareResponse paymentPrepareResponse = paymentService.preparePayment(customUserDetails.getId(), paymentPrepareRequest.getPlanType());

        return ResponseEntity.ok(ApiResponse.success("결제 준비 완료", paymentPrepareResponse));
    }

    @PostMapping("/{paymentId}/complete")
    public ResponseEntity<ApiResponse<PaymentCompleteResponse>> complete(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                          @PathVariable String paymentId){
        PaymentCompleteResponse paymentCompleteResponse = paymentService.completePayment(paymentId, customUserDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("결제 완료 확인", paymentCompleteResponse));
    }

}
