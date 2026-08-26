package com.likelion.dev_community.domain.user.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.user.dto.authDto.*;
import com.likelion.dev_community.domain.user.service.AuthService;
import com.likelion.dev_community.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "회원가입, 로그인/로그아웃, 토큰 재발급, 아이디/닉네임/이메일 중복 확인, 소셜 로그인 가입 완료, 비밀번호 재설정을 다루는 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "아이디/비밀번호/닉네임/이메일로 자체 계정을 생성. 인증 불필요.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest signUpRequest){
        SignUpResponse signUpResponse = authService.signUp(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("회원가입 성공",signUpResponse));
    }

    @Operation(summary = "로그인", description = "아이디/비밀번호로 로그인해 액세스 토큰을 발급받음. 리프레시 토큰은 HttpOnly 쿠키로 전달됨. 인증 불필요.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody SignInRequest request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse
    ){
        TokenResponse tokenResponse = authService.signIn(request, httpServletRequest, httpServletResponse);
        return ResponseEntity.ok(ApiResponse.success("로그인 성공",tokenResponse));
    }

    @Operation(summary = "액세스 토큰 재발급", description = "쿠키에 담긴 리프레시 토큰으로 새 액세스 토큰을 발급받음. 리프레시 토큰이 없으면 401.")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<ReissueResponse>> reissue(@CookieValue(value = "refreshToken", required = false) String refreshToken){
        if(refreshToken==null)
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);

        ReissueResponse reissueResponse = authService.reissue(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("토큰 재발급 성공",reissueResponse));
    }

    @Operation(summary = "로그아웃", description = "본인의 리프레시 토큰을 서버(Redis)에서 삭제하고 쿠키를 만료시킴.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletResponse httpServletResponse){
        authService.logout(userDetails.getId(), httpServletResponse);
        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공",null));
    }

    @Operation(summary = "아이디 중복 확인", description = "회원가입 전 아이디 사용 가능 여부를 확인. 인증 불필요.")
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Void>> checkUsername(@RequestParam @NotBlank String username){
        authService.checkUsername(username);
        return ResponseEntity.ok(ApiResponse.success("사용 가능한 아이디입니다", null));
    }

    @Operation(summary = "닉네임 중복 확인", description = "회원가입/정보수정 전 닉네임 사용 가능 여부를 확인. 인증 불필요.")
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<Void>> checkNickname(@RequestParam @NotBlank String nickname){
        authService.checkNickname(nickname);
        return ResponseEntity.ok(ApiResponse.success("사용 가능한 닉네임입니다", null));
    }

    @Operation(summary = "이메일 중복 확인", description = "회원가입 전 이메일 사용 가능 여부를 확인. 자체가입/소셜가입 계정을 통합해 중복 검사. 인증 불필요.")
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Void>> checkEmail(@RequestParam @NotBlank String email){
        authService.checkEmail(email);
        return ResponseEntity.ok(ApiResponse.success("사용 가능한 이메일입니다", null));
    }

    @Operation(summary = "소셜 로그인 회원가입 완료(닉네임 등록)", description = "GitHub/Google 최초 로그인 후 발급받은 signupToken으로 닉네임(및 provider가 이메일을 못 준 경우 이메일)을 확정해 계정을 실제로 생성하고 로그인 처리. 인증 불필요.")
    @PostMapping("/oauth/complete")
    public ResponseEntity<ApiResponse<TokenResponse>> oauthComplete(
            @Valid @RequestBody OAuthCompleteRequest request,
            HttpServletResponse httpServletResponse
    ) {
        TokenResponse tokenResponse = authService.oauthComplete(request.getSignupToken(), request.getNickname(), request.getEmail(), httpServletResponse);
        return ResponseEntity.ok(ApiResponse.success("회원가입 및 로그인 성공", tokenResponse));
    }

    @Operation(summary = "비밀번호 재설정 요청", description = "아이디+이메일이 일치하면 재설정 링크를 이메일로 발송. 계정 존재 여부를 노출하지 않기 위해 불일치해도 항상 동일한 성공 메시지를 반환.")
    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request.getUsername(), request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("입력하신 정보와 일치하는 계정이 있다면 이메일로 재설정 링크를 보냈습니다.", null));
    }

    @Operation(summary = "비밀번호 재설정 확정", description = "이메일로 받은 재설정 토큰과 새 비밀번호로 비밀번호를 변경. 토큰은 1회용이며 30분 후 만료됨.")
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 재설정되었습니다.", null));
    }
}