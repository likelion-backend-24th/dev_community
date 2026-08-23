package com.likelion.dev_community.domain.user.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.user.dto.authDto.*;
import com.likelion.dev_community.domain.user.service.AuthService;
import com.likelion.dev_community.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest signUpRequest){
        SignUpResponse signUpResponse = authService.signUp(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("회원가입 성공",signUpResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody SignInRequest request, HttpServletResponse httpServletResponse){
        TokenResponse tokenResponse = authService.signIn(request, httpServletResponse);
        return ResponseEntity.ok(ApiResponse.success("로그인 성공",tokenResponse));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<ReissueResponse>> reissue(@CookieValue(value = "refreshToken", required = false) String refreshToken){
        if(refreshToken==null)
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);

        ReissueResponse reissueResponse = authService.reissue(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("토큰 재발급 성공",reissueResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletResponse httpServletResponse){
        authService.logout(userDetails.getId(), httpServletResponse);
        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공",null));
    }

    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Void>> checkUsername(@RequestParam @NotBlank String username){
        authService.checkUsername(username);
        return ResponseEntity.ok(ApiResponse.success("사용 가능한 아이디입니다", null));
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<Void>> checkNickname(@RequestParam @NotBlank String nickname){
        authService.checkNickname(nickname);
        return ResponseEntity.ok(ApiResponse.success("사용 가능한 닉네임입니다", null));
    }

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Void>> checkEmail(@RequestParam @NotBlank String email){
        authService.checkEmail(email);
        return ResponseEntity.ok(ApiResponse.success("사용 가능한 이메일입니다", null));
    }

    @PostMapping("/oauth/complete")
    public ResponseEntity<ApiResponse<TokenResponse>> oauthComplete(
            @Valid @RequestBody OAuthCompleteRequest request,
            HttpServletResponse httpServletResponse
    ) {
        TokenResponse tokenResponse = authService.oauthComplete(request.getSignupToken(), request.getNickname(), request.getEmail(), httpServletResponse);
        return ResponseEntity.ok(ApiResponse.success("회원가입 및 로그인 성공", tokenResponse));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request.getUsername(), request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("입력하신 정보와 일치하는 계정이 있다면 이메일로 재설정 링크를 보냈습니다.", null));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 재설정되었습니다.", null));
    }
}