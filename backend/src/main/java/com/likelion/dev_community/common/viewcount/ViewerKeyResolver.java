package com.likelion.dev_community.common.viewcount;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class ViewerKeyResolver {

    public String resolve(Long userId, HttpServletRequest request) {
        if (userId != null) {
            return "user:" + userId;
        }
        return "ip:" + extractIp(request);
    }

    public String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}