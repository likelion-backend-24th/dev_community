package com.likelion.dev_community.domain.user.entity;

import com.likelion.dev_community.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    @Column(unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(length = 50)
    private String providerId;
    @Column(nullable = false)
    private int reputation;

    @Column(nullable = false)
    private boolean isExpert;

    @Column(nullable = false)
    private boolean expertRequested;

    // 멤버십 전용 아바타 색상. null이면 기본 파란색(비멤버/미추첨 상태)으로 표시된다.
    @Column(length = 10)
    private String avatarColorHex;

    // 마지막으로 색을 뽑은 시각. 결제 갱신(Subscription.startedAt)마다 1회로 리롤을 제한하는 기준이 된다.
    private LocalDateTime avatarColorRolledAt;

    // 가장 최근 로그인 시각/IP. 직전 값만 보관하며(이력 미보관), 로그인 성공마다 덮어쓴다.
    private LocalDateTime lastLoginAt;

    @Column(length = 45)
    private String lastLoginIp;

    @Builder
    public User(String username, String password, String nickname, String email, Role role, UserStatus status,
                AuthProvider provider, String providerId) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
        this.status = status;
        this.provider = provider;
        this.providerId = providerId;
    }

    public static User createUser(String username, String encodedPassword, String nickname, String email) {
        return User.builder()
                .username(username)
                .password(encodedPassword)
                .nickname(nickname)
                .email(email)
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .provider(AuthProvider.LOCAL)
                .build();
    }

    public static User createOAuthUser(String username, String encodedRandomPassword, String nickname, String email,
                                       AuthProvider provider, String providerId) {
        return User.builder()
                .username(username)
                .password(encodedRandomPassword)
                .nickname(nickname)
                .email(email)
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .provider(provider)
                .providerId(providerId)
                .build();
    }

    public void updateUser(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    public void unsuspend() {
        this.status = UserStatus.ACTIVE;
    }

    public String getDisplayNickname() {
        return this.status == UserStatus.WITHDRAWN ? "탈퇴한 사용자" : this.nickname;
    }

    public void increaseReputation(int points) {
        this.reputation += points;
    }

    public void decreaseReputation(int points) {
        this.reputation = Math.max(0, this.reputation - points);
    }

    public void grantExpert() {
        this.isExpert = true;
        this.expertRequested = false;
    }

    public void revokeExpert() {
        this.isExpert = false;
    }

    public void requestExpert() {
        this.expertRequested = true;
    }

    public void cancelExpertRequest() {
        this.expertRequested = false;
    }

    public void rollAvatarColor(String hex, LocalDateTime rolledAt) {
        this.avatarColorHex = hex;
        this.avatarColorRolledAt = rolledAt;
    }

    public void recordLogin(String ip, LocalDateTime at) {
        this.lastLoginAt = at;
        this.lastLoginIp = ip;
    }
}
