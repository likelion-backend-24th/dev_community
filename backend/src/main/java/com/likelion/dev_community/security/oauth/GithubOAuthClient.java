package com.likelion.dev_community.security.oauth;

import tools.jackson.databind.JsonNode;
import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.user.dto.oauthDto.GithubUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GithubOAuthClient {

    private final RestClient restClient = RestClient.create();

    @Value("${oauth.github.client-id}")
    private String clientId;

    @Value("${oauth.github.client-secret}")
    private String clientSecret;

    @Value("${oauth.github.redirect-uri}")
    private String redirectUri;

    public GithubUserInfo getUserInfo(String code) {
        String githubAccessToken = requestAccessToken(code);
        return requestUserInfo(githubAccessToken);
    }

    private String requestAccessToken(String code) {
        try {
            JsonNode response = restClient.post()
                    .uri("https://github.com/login/oauth/access_token")
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new TokenRequestBody(clientId, clientSecret, code, redirectUri))
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || response.get("access_token") == null) {
                throw new CustomException(ErrorCode.OAUTH_LOGIN_FAILED, "GitHub 토큰 발급에 실패했습니다.");
            }
            return response.get("access_token").asText();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.OAUTH_LOGIN_FAILED, "GitHub 인증 처리 중 오류가 발생했습니다.");
        }
    }

    private GithubUserInfo requestUserInfo(String accessToken) {
        try {
            JsonNode response = restClient.get()
                    .uri("https://api.github.com/user")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || response.get("id") == null) {
                throw new CustomException(ErrorCode.OAUTH_LOGIN_FAILED, "GitHub 사용자 정보를 가져오지 못했습니다.");
            }

            return new GithubUserInfo(response.get("id").asText(), response.get("login").asText());
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.OAUTH_LOGIN_FAILED, "GitHub 사용자 정보를 가져오지 못했습니다.");
        }
    }

    private record TokenRequestBody(String client_id, String client_secret, String code, String redirect_uri) {}
}