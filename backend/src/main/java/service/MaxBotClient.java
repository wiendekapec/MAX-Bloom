package service;

import dto.bot.MaxInlineButtonDto;
import dto.bot.MaxSendMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Клиент для взаимодействия с API платформы MAX.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaxBotClient {

    private final RestTemplate restTemplate;

    @Value("${max.bot.token:}")
    private String botToken;

    @Value("${max.bot.api-base:https://api.max.ru/bot}")
    private String maxApiBase;

    /**
     * Отправка текстового сообщения.
     */
    public void sendMessage(String maxUserId, String text) {
        sendMessage(maxUserId, text, null);
    }

    /**
     * Отправка сообщения с инлайн-кнопками.
     */
    public void sendMessage(String maxUserId, String text, List<List<MaxInlineButtonDto>> keyboard) {
        MaxSendMessageRequest body = MaxSendMessageRequest.builder()
                .userId(Long.parseLong(maxUserId))
                .text(text)
                .inlineKeyboard(keyboard)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + botToken);

        HttpEntity<MaxSendMessageRequest> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(
                    maxApiBase + "/messages",
                    request,
                    Void.class
            );
            log.debug("Sent message to userId={}", maxUserId);
        } catch (Exception e) {
            log.error("Failed to send MAX message to userId={}: {}", maxUserId, e.getMessage(), e);
        }
    }

    /**
     * Проверка наличия прав на управление участниками в чате.
     */
    public boolean hasMemberManagementRight(String chatId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + botToken);
        HttpEntity<?> req = new HttpEntity<>(headers);

        try {
            ResponseEntity<java.util.Map> resp = restTemplate.exchange(
                    maxApiBase + "/chats/" + chatId + "/members/me",
                    HttpMethod.GET, req, java.util.Map.class
            );
            java.util.Map<?, ?> body = resp.getBody();
            if (body == null) return false;
            Object permissions = body.get("permissions");
            if (permissions instanceof java.util.List<?> perms) {
                return perms.contains("add_remove_members");
            }
            return false;
        } catch (Exception e) {
            log.warn("Could not check member rights chatId={}: {}", chatId, e.getMessage());
            return false;
        }
    }

    /**
     * Получение ссылки-приглашения чата.
     */
    public String getChatInviteLink(String chatId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + botToken);
        HttpEntity<?> req = new HttpEntity<>(headers);

        try {
            ResponseEntity<java.util.Map> resp = restTemplate.exchange(
                    maxApiBase + "/chats/" + chatId,
                    HttpMethod.GET, req, java.util.Map.class
            );
            java.util.Map<?, ?> body = resp.getBody();
            return body != null ? (String) body.get("link") : null;
        } catch (Exception e) {
            log.error("Could not get invite link for chatId={}: {}", chatId, e.getMessage());
            return null;
        }
    }
}
