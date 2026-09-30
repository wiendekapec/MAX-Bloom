package service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Валидатор подписи Telegram / MAX Web App initData.
 */
@Slf4j
@Service
public class MaxInitDataVerifier {

    @Value("${max.bot.secret:}")
    private String botSecret;

    /**
     * Проверка подписи initData и извлечение идентификатора пользователя.
     */
    public String verifyAndExtractUserId(String initData) {
        if (initData == null || initData.isBlank()) {
            throw new SecurityException("UNAUTHORIZED_INIT_DATA: missing header");
        }

        if (botSecret == null || botSecret.isBlank() || "replace_me_secret".equals(botSecret)) {
            log.warn("MaxInitData HMAC verification disabled (no bot secret configured)");
            return extractUserIdUnsafe(initData);
        }

        try {
            Map<String, String> params = parseParams(initData);
            String hash = params.remove("hash");
            if (hash == null) {
                throw new SecurityException("UNAUTHORIZED_INIT_DATA: no hash");
            }

            String authDateStr = params.get("auth_date");
            if (authDateStr != null && !authDateStr.isBlank()) {
                try {
                    long authDate = Long.parseLong(authDateStr);
                    long now = Instant.now().getEpochSecond();

                    if (now - authDate > 86400 || (authDate - now) > 60) {
                        throw new SecurityException("UNAUTHORIZED_INIT_DATA: auth_date expired or invalid");
                    }
                } catch (NumberFormatException e) {
                    throw new SecurityException("UNAUTHORIZED_INIT_DATA: malformed auth_date");
                }
            }

            String checkString = params.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("");

            byte[] secretKey = hmacSha256("WebAppData".getBytes(StandardCharsets.UTF_8),
                    botSecret.getBytes(StandardCharsets.UTF_8));
            byte[] expectedHash = hmacSha256(checkString.getBytes(StandardCharsets.UTF_8), secretKey);
            String expectedHex = bytesToHex(expectedHash);

            byte[] expectedBytes = expectedHex.getBytes(StandardCharsets.UTF_8);
            byte[] actualBytes = hash.getBytes(StandardCharsets.UTF_8);
            if (!MessageDigest.isEqual(expectedBytes, actualBytes)) {
                throw new SecurityException("UNAUTHORIZED_INIT_DATA: invalid hash");
            }

            String userJson = params.get("user");
            return extractUserIdFromJson(userJson);

        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            log.error("InitData verification error: {}", e.getMessage(), e);
            throw new SecurityException("UNAUTHORIZED_INIT_DATA: " + e.getMessage());
        }
    }

    private String extractUserIdUnsafe(String initData) {
        try {
            Map<String, String> params = parseParams(initData);
            String userJson = params.get("user");
            if (userJson != null) return extractUserIdFromJson(userJson);

            return params.getOrDefault("user_id", "dev_user");
        } catch (Exception e) {
            return "dev_user";
        }
    }

    private String extractUserIdFromJson(String userJson) {
        if (userJson == null) throw new IllegalArgumentException("No user in initData");
        String decoded;
        try {
            decoded = URLDecoder.decode(userJson, StandardCharsets.UTF_8);
        } catch (Exception e) {
            decoded = userJson;
        }

        int idxId = decoded.indexOf("\"id\":");
        if (idxId < 0) throw new IllegalArgumentException("No id field in user JSON");
        int start = idxId + 5;
        int end = start;
        while (end < decoded.length() && (Character.isDigit(decoded.charAt(end)) || decoded.charAt(end) == '-')) {
            end++;
        }
        return decoded.substring(start, end).trim();
    }

    private Map<String, String> parseParams(String initData) throws Exception {
        Map<String, String> map = new LinkedHashMap<>();
        for (String pair : initData.split("&")) {
            int eq = pair.indexOf('=');
            if (eq < 0) continue;
            String key = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
            map.put(key, value);
        }
        return map;
    }

    private byte[] hmacSha256(byte[] data, byte[] key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
