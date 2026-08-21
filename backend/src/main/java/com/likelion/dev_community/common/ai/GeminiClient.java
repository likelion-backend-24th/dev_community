package com.likelion.dev_community.common.ai;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 질문 글 AI 요약/태그 추천용 Gemini API 클라이언트. 무료 티어(과금 없이 rate-limit만 있는)로 호출한다.
@Component
public class GeminiClient {

    private final RestClient restClient = RestClient.create();
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    public String summarize(String title, String content) {
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

        String text = callGemini(prompt, ErrorCode.AI_SUMMARY_FAILED);
        return text.trim();
    }

    // 질문 작성 중 본문 기준으로 기술 스택 위주 태그를 추천한다. 최대 5개, 적으면 그보다 적게 반환될 수 있다.
    public List<String> suggestTags(String title, String content) {
        String prompt = """
                너는 개발자 커뮤니티에 올라온 질문 글을 읽고 태그를 추천하는 어시스턴트야. 아래 [제목]과 [본문]을 읽고,
                이 글에 달면 좋을 기술 스택 위주의 태그를 추천해줘.

                다음을 지켜줘.
                - 프로그래밍 언어, 프레임워크, 라이브러리, 데이터베이스, 인프라/툴 이름처럼 구체적인 기술 스택 명칭 위주로 추천해줘.
                  "버그", "질문", "도와주세요" 같은 추상적이거나 감정적인 단어는 태그로 추천하지 마.
                - 실제로 본문 내용과 관련 있는 태그만 추천해줘. 최대 5개까지 가능하지만, 억지로 5개를 채우지 말고
                  본문과 명확히 관련된 것만 그보다 적게 반환해도 돼. 본문에서 기술 스택을 전혀 특정할 수 없으면 빈 배열을 반환해.
                - 각 태그는 널리 쓰이는 표기법(예: Spring Boot, JPA, React, MySQL)으로, 한 태그당 2~3단어 이내로 짧게 적어줘.
                - 출력은 오직 JSON 문자열 배열 하나여야 해. 예: ["Spring Boot", "JPA", "MySQL"]
                  다른 설명, 마크다운 코드블록, 줄바꿈 있는 텍스트는 절대 출력하지 마.

                [제목]
                %s

                [본문]
                %s
                """.formatted(title, content);

        String text = callGemini(prompt, ErrorCode.AI_TAG_SUGGESTION_FAILED);
        return parseTagArray(text);
    }

    private String callGemini(String prompt, ErrorCode failureCode) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new CustomException(failureCode, "Gemini API 키가 설정되지 않았습니다.");
        }

        try {
            JsonNode response = restClient.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={apiKey}",
                            model, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))))
                    .retrieve()
                    .body(JsonNode.class);

            String text = extractText(response);
            if (text == null || text.isBlank()) {
                throw new CustomException(failureCode);
            }
            return text;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(failureCode);
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

    // 모델이 JSON 배열 앞뒤에 마크다운 코드펜스나 설명을 붙이는 경우가 있어, 첫 '['와 마지막 ']' 사이만 잘라 파싱한다.
    private List<String> parseTagArray(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start == -1 || end == -1 || end < start) {
            throw new CustomException(ErrorCode.AI_TAG_SUGGESTION_FAILED);
        }

        try {
            JsonNode array = jsonMapper.readTree(text.substring(start, end + 1));
            List<String> tags = new ArrayList<>();
            array.forEach(node -> {
                String tag = node.asText(null);
                if (tag != null && !tag.isBlank()) {
                    tags.add(tag.trim());
                }
            });
            return tags;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AI_TAG_SUGGESTION_FAILED);
        }
    }
}
