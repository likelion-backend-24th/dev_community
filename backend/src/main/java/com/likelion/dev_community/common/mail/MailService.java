package com.likelion.dev_community.common.mail;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.user.entity.AuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

// spring.mail.username이 비어있으면(로컬 등 미설정 환경) 메일을 보내지 않고 로그만 남긴다.
@Slf4j
@Component
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendPasswordResetEmail(String to, String resetUrl) {
        send(to, "[Dev_Community] 비밀번호 재설정 안내",
                "비밀번호 재설정을 요청하셨습니다. 아래 링크에서 새 비밀번호를 설정해주세요. (30분간 유효)\n\n"
                        + resetUrl
                        + "\n\n본인이 요청하지 않았다면 이 메일은 무시하셔도 됩니다.");
    }

    public void sendSocialAccountNotice(String to, AuthProvider provider) {
        send(to, "[Dev_Community] 비밀번호 재설정 안내",
                provider.name() + " 소셜 로그인으로 가입된 계정이라 별도 비밀번호가 없습니다. "
                        + "로그인 화면에서 " + provider.name() + " 로그인을 이용해주세요.");
    }

    private void send(String to, String subject, String text) {
        if (fromAddress == null || fromAddress.isBlank()) {
            log.warn("spring.mail.username이 설정되지 않아 메일을 보내지 않습니다. to={}", to);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("메일 발송 실패 - to={}", to, e);
            throw new CustomException(ErrorCode.PASSWORD_RESET_EMAIL_FAILED);
        }
    }
}
