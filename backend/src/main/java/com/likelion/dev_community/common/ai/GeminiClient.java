package com.likelion.dev_community.common.ai;

import tools.jackson.databind.JsonNode;
import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

// 질문 글 AI 요약용 Gemini API 클라이언트. 무료 티어(과금 없이 rate-limit만 있는)로 호출한다.
@Component
public class GeminiClient {

    private final RestClient restClient = RestClient.create();

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    public String summarize(String title, String content) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new CustomException(ErrorCode.AI_SUMMARY_FAILED, "Gemini API 키가 설정되지 않았습니다.");
        }

        String prompt = """
                너는 개발자 커뮤니티에 올라온 질문 글을 요약하는 어시스턴트야. 아래 [제목]과 [본문]을 읽고,
                한국어 3문장 이내(150자 안팎)로 요약해줘.

                요약에는 다음 세 가지를 반드시 포함해줘.
                - 작성자가 무엇을 하려던 상황인지
                - 실제로 겪고 있는 문제/에러가 무엇인지
                - 답변자에게 구체적으로 무엇을 알고 싶어하는지

                다음은 지켜줘.
                - 요약 결과의 모든 설명과 서술은 반드시 한국어로만 작성해줘. (단, 코드명, 변수명, 라이브러리명, 에러 메시지 같은 고유명사는 번역하지 말고 원문 그대로 유지해)
                - 질문글에 잘못된 기술적 전제, 문법 에러, 논리적 버그, 틀린 용어가 있어도 임의로 고치거나 보정하지 말고, 작성자가 쓴 내용 그대로 반영해서 요약해줘.
                  코드를 디버깅하거나 최적화하지 말고, 작성자가 시도한 작업과 에러가 발생한다고 주장하는 지점만 요약해줘.
                - 인사말이나 배경 설명이 장황해도 핵심만 추리고, 코드 블록은 있는 그대로 옮기지 말고
                  어떤 문제를 일으키는 코드인지만 짧게 언급해줘
                - 요약 결과 문장 외의 다른 말(설명, 인사, 머리말)은 절대 출력하지 마

                [제목]
                %s

                [본문]
                %s
                """.formatted(title, content);

        try {
            JsonNode response = restClient.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={apiKey}",
                            model, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))))
                    .retrieve()
                    .body(JsonNode.class);

            String summary = extractText(response);
            if (summary == null || summary.isBlank()) {
                throw new CustomException(ErrorCode.AI_SUMMARY_FAILED);
            }
            return summary.trim();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AI_SUMMARY_FAILED);
        }
    }

    private String extractText(JsonNode response) {
        if (response == null) return null;
        JsonNode candidates = response.get("candidates");
        if (candidates == null || !candidates.isArray() || candidates.isEmpty()) return null;
        JsonNode parts = candidates.get(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) return null;
        return parts.get(0).path("text").asText(null);
    }
}
