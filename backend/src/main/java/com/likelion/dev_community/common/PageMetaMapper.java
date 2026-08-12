package com.likelion.dev_community.common;

import org.springframework.data.domain.Page;

import java.util.Map;

public final class PageMetaMapper {

    private PageMetaMapper() {
    }

    public static Map<String, Object> of(Page<?> page) {
        return Map.of(
                "page", page.getNumber(),
                "size", page.getSize(),
                "totalElements", page.getTotalElements(),
                "totalPages", page.getTotalPages()
        );
    }
}
