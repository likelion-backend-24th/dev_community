package com.likelion.dev_community.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// application-prod.yml의 spring.datasource.url이 YAML 문법상 올바르게 파싱되고,
// DB_NAME/DB_SSL_MODE 등 신규 플레이스홀더가 기존 기본값(dev_community/PREFERRED)을
// 유지하는 형태로 남아있는지 확인한다. 실제 DB 연결 없이 검증하기 위한 테스트라
// 전체 Spring 컨텍스트를 띄우지 않고 YAML 로더만 사용한다.
class ProdDatasourceUrlTest {

    @Test
    void datasource_url이_YAML로_정상_파싱되고_기본값을_포함한다() throws Exception {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        List<PropertySource<?>> sources = loader.load(
                "application-prod.yml", new ClassPathResource("application-prod.yml"));

        String url = (String) sources.get(0).getProperty("spring.datasource.url");

        assertThat(url).isNotNull();
        assertThat(url).startsWith("jdbc:mysql://${DB_HOST}:${DB_PORT:3306}/");
        assertThat(url).contains("${DB_NAME:dev_community}");
        assertThat(url).contains("sslMode=${DB_SSL_MODE:PREFERRED}");
        assertThat(url).contains("serverTimezone=Asia/Seoul");
    }
}
