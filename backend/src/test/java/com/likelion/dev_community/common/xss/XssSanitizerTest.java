package com.likelion.dev_community.common.xss;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XssSanitizerTest {

    private final XssSanitizer sanitizer = new XssSanitizer();

    @Test
    void null_입력은_null을_반환한다() {
        assertThat(sanitizer.sanitize(null)).isNull();
    }

    @Test
    void 빈_문자열은_그대로_반환한다() {
        assertThat(sanitizer.sanitize("")).isEqualTo("");
    }

    @Test
    void 특수문자가_없는_일반_텍스트는_그대로_반환한다() {
        assertThat(sanitizer.sanitize("안녕하세요 Spring Boot")).isEqualTo("안녕하세요 Spring Boot");
    }

    @Test
    void script_태그를_이스케이프한다() {
        String result = sanitizer.sanitize("<script>alert('xss')</script>");

        assertThat(result).isEqualTo("&lt;script&gt;alert(&#39;xss&#39;)&lt;&#x2F;script&gt;");
        assertThat(result).doesNotContain("<script>");
    }

    @Test
    void img_onerror_속성을_이스케이프한다() {
        String result = sanitizer.sanitize("<img src=x onerror=\"alert('xss')\">");

        assertThat(result).doesNotContain("<img");
        assertThat(result).contains("&lt;img");
    }

    @Test
    void 앰퍼샌드를_먼저_이스케이프해서_이중_이스케이프되지_않는다() {
        // &를 마지막에 치환하면 이미 만들어진 &lt; 등이 &amp;lt;로 이중 이스케이프됨
        String result = sanitizer.sanitize("A & B < C");

        assertThat(result).isEqualTo("A &amp; B &lt; C");
    }

    @Test
    void 슬래시를_이스케이프한다() {
        assertThat(sanitizer.sanitize("a/b")).isEqualTo("a&#x2F;b");
    }
}
