package com.familyos.familyos.demo.mapper;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
public class DemoEmailMimeMapper {

    public String toBase64UrlMime(Map<String, Object> emailDef) {
        StringBuilder mime = new StringBuilder();
        mime.append("MIME-Version: 1.0\r\n");

        String receivedAt = (String) emailDef.getOrDefault("receivedAt", "2026-07-26T14:00:00-05:00");
        mime.append("Date: ").append(receivedAt).append("\r\n");

        Object fromObj = emailDef.get("from");
        if (fromObj instanceof Map<?, ?> from) {
            mime.append("From: ").append(from.get("name")).append(" <").append(from.get("email")).append(">\r\n");
        }

        Object toObj = emailDef.get("to");
        if (toObj instanceof java.util.List<?> toList && !toList.isEmpty()) {
            mime.append("To: ").append(String.join(", ", toList.stream().map(Object::toString).toList())).append("\r\n");
        } else {
            mime.append("To: parentosfamily@gmail.com\r\n");
        }

        mime.append("Subject: ").append(emailDef.getOrDefault("subject", "(no subject)")).append("\r\n");
        mime.append("Content-Type: text/plain; charset=UTF-8\r\n");
        mime.append("\r\n");
        mime.append(emailDef.getOrDefault("bodyText", emailDef.getOrDefault("snippet", "")));

        byte[] bytes = mime.toString().getBytes(StandardCharsets.UTF_8);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
