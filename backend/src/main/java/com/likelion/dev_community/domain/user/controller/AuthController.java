package com.likelion.dev_community.domain.user.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.user.dto.authDto.*;
import com.likelion.dev_community.domain.user.service.AuthService;
import com.likelion.dev_community.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest signUpRequest){
        SignUpResponse signUpResponse = authService.signUp(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("회원가입 성공",signUpResponse));
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody SignInRequest request, HttpServletResponse httpServletResponse){
        TokenResponse tokenResponse = authService.signIn(request, httpServletResponse);
        return ResponseEntity.ok(ApiResponse.success("로그인 성공",tokenResponse));
    }

    @Operation(summary = "액세스 토큰 재발급")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<ReissueResponse>> reissue(@CookieValue(value = "refreshToken", required = false) String refreshToken){
        if(refreshToken==null)
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);

        ReissueResponse reissueResponse = authService.reissue(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("토큰 재발급 성공",reissueResponse));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletResponse httpServletResponse){
        authService.logout(userDetails.getId(), httpServletResponse);
        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공",null));
    }

    @Operation(summary = "아이디 중복 확인")
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Void>> checkUsername(@RequestParam @NotBlank String username){
        authService.checkUsername(username);
        return ResponseEntity.ok(ApiResponse.success("사용 가능한 아이디입니다", null));
    }

    @Operation(summary = "닉네임 중복 확인")
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<Void>> checkNickname(@RequestParam @NotBlank String nickname){
        authService.checkNickname(nickname);
        return ResponseEntity.ok(ApiResponse.success("사용 가능한 닉네임입니다", null));
    }

    @Operation(summary = "소셜 로그인 회원가입 완료(닉네임 등록)")
    @PostMapping("/oauth/complete")
    public ResponseEntity<ApiResponse<TokenResponse>> oauthComplete(
            @Valid @RequestBody OAuthCompleteRequest request,
            HttpServletResponse httpServletResponse
    ) {
        TokenResponse tokenResponse = authService.oauthComplete(request.getSignupToken(), request.getNickname(), httpServletResponse);
        return ResponseEntity.ok(ApiResponse.success("회원가입 및 로그인 성공", tokenResponse));
    }
}