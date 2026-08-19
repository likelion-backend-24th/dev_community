package com.likelion.dev_community.domain.user.dto.oauthDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GithubUserInfo {
    private final String id;
    private final String login;
}