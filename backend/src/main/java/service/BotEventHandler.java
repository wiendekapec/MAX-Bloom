package service;

import database.entity.*;
import database.repository.UserRepository;
import dto.bot.MaxInlineButtonDto;
import dto.community.CommunityCategory;
import dto.payment.CreatePaymentResponse;
import dto.subscription.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Обработчик входящих событий и команд бота.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BotEventHandler {

    private final UserService userService;
    private final BotSessionService sessionService;
    private final CommunityService communityService;
    private final SubscriptionService subscriptionService;
    private final PaymentService paymentService;
    private final MaxBotClient botClient;

    @Value("${bloom.app-base-url}")
    private String appBaseUrl;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter
            .ofPattern("dd.MM.yyyy").withZone(ZoneId.of("Europe/Moscow"));

    /**
     * Обработка входящего текстового сообщения от пользователя.
     */
    public void handleMessage(dto.bot.MaxUserDto senderDto, String text) {
        User user = userService.findOrCreate(senderDto);
        String maxUserId = user.getMaxUserId();

        if (text == null || text.isBlank() || "/start".equals(text.trim())) {
            sessionService.clear(maxUserId);
            sendMainMenu(maxUserId, user.getFirstName());
            return;
        }

        String trimmed = text.trim();

        if (trimmed.startsWith("/start plan_")) {
            sessionService.clear(maxUserId);
            handleStartPlan(user, trimmed.substring("/start plan_".length()));
            return;
        }

        if (trimmed.equals("/start business")) {
            sessionService.clear(maxUserId);
            startSellerOnboarding(user);
            return;
        }

        BotSessionService.State state = sessionService.getState(maxUserId);
        if (state != BotSessionService.State.ONBOARDING_INFO) {
            BotSessionService.Draft draft = sessionService.getDraft(maxUserId);
            handleFsmText(user, state, draft, trimmed);
        } else {
            botClient.sendMessage(maxUserId, "Не понял. Используй меню ниже.", mainMenuButtons());
        }
    }

    /**
     * Обработка нажатия инлайн-кнопки (callback).
     */
    public void handleCallback(dto.bot.MaxUserDto senderDto, String payload) {
        User user = userService.findOrCreate(senderDto);
        String maxUserId = user.getMaxUserId();

        String[] parts = payload.split(":", 3);
        if (parts.length < 2 || !"action".equals(parts[0])) {
            log.warn("Unknown callback payload: {}", payload);
            return;
        }
        String action = parts[1];
        String arg = parts.length > 2 ? parts[2] : "";

        switch (action) {
            case "menu" -> { sessionService.clear(maxUserId); sendMainMenu(maxUserId, user.getFirstName()); }
            case "role" -> { if ("seller".equals(arg)) startSellerOnboarding(user); else sendMainMenu(maxUserId, user.getFirstName()); }
            case "selfemp" -> { if ("confirm".equals(arg)) handleSelfEmpConfirm(user); }
            case "category" -> {
                BotSessionService.Draft draft = sessionService.getDraft(maxUserId);
                handleCategorySelected(user, draft, arg);
            }
            case "period" -> {
                BotSessionService.Draft draft = sessionService.getDraft(maxUserId);
                handlePeriodSelected(user, draft, arg);
            }
            case "renew"         -> handleRenew(user, Long.parseLong(arg));
            case "resend_invite" -> handleResendInvite(user, Long.parseLong(arg));
            case "retry_payment" -> handleRetryPayment(user, Long.parseLong(arg));
            case "my_subs"       -> handleMySubscriptions(user);
            case "catalog"       -> botClient.sendMessage(maxUserId, "Открываю каталог...");
            case "dashboard"     -> botClient.sendMessage(maxUserId, "Открываю дашборд...");
            default -> log.warn("Unknown action={} userId={}", action, maxUserId);
        }
    }

    /**
     * Обработка добавления бота в канал или чат.
     */
    public void handleBotAdded(String chatId) {
        log.info("bot_added chatId={}", chatId);

        Optional<BotSessionService.PendingChatEntry> entryOpt = sessionService.findByPendingChat(chatId);
        if (entryOpt.isEmpty()) {
            log.info("No pending seller session for chatId={}", chatId);
            return;
        }

        BotSessionService.PendingChatEntry entry = entryOpt.get();
        String maxUserId = entry.getMaxUserId();
        BotSessionService.Draft draft = entry.getDraft();

        if (!botClient.hasMemberManagementRight(chatId)) {
            botClient.sendMessage(maxUserId,
                    "⚠️ Дайте боту право управлять участниками чата.\n" +
                    "После добавления права попробуйте снова: /start business");
            return;
        }

        String inviteLink = botClient.getChatInviteLink(chatId);
        User creator;
        try {
            creator = userService.getByMaxUserId(maxUserId);
        } catch (Exception e) {
            log.error("Creator not found userId={}", maxUserId);
            return;
        }

        try {
            Community community = communityService.finalizeCommunity(
                    creator, chatId,
                    draft.getTitle(), draft.getDescription(), draft.getCategory(),
                    inviteLink,
                    draft.getPlanTitle(), draft.getPlanDescription(),
                    draft.getPlanPrice(), draft.getPlanPeriodDays()
            );
            sessionService.clear(maxUserId);

            Long firstPlanId = community.getPlans().isEmpty() ? 0L : community.getPlans().get(0).getId();
            String deepLink = "https://max.ru/bot?start=plan_" + firstPlanId;
            botClient.sendMessage(maxUserId,
                    "🎉 Тариф опубликован!\nСсылка для покупателей:\n" + deepLink,
                    List.of(List.of(
                            btn("📊 Дашборд", "action:dashboard"),
                            btn("➕ Ещё тариф", "action:role:seller")
                    ))
            );
        } catch (Exception e) {
            log.error("finalizeCommunity failed chatId={}: {}", chatId, e.getMessage(), e);
            botClient.sendMessage(maxUserId, "❌ Не удалось привязать канал. Попробуй /start business снова.");
        }
    }

    /**
     * Уведомление об успешной оплате подписки.
     */
    public void notifyPaymentSucceeded(Payment payment) {
        notifyPaymentSucceeded(payment, null, null);
    }

    /**
     * Уведомление об успешной оплате с передачей активированной подписки и токена.
     */
    public void notifyPaymentSucceeded(Payment payment, Subscription sub, InviteToken token) {
        User user = payment.getUser();
        if (sub == null) {
            sub = subscriptionService.activate(user, payment);
        }
        if (token == null) {
            token = subscriptionService.createToken(sub);
        }

        String inviteUrl = appBaseUrl + "/i/" + token.getToken();
        String expires = sub.getExpiresAt() != null ? DATE_FMT.format(sub.getExpiresAt()) : "бессрочно";

        botClient.sendMessage(user.getMaxUserId(),
                "✅ Оплата прошла! Подписка до: " + expires + "\n\nВаша ссылка (24 ч):\n" + inviteUrl,
                List.of(List.of(btn("Вступить →", inviteUrl)))
        );
    }

    /**
     * Уведомление об отмене оплаты.
     */
    public void notifyPaymentCanceled(Payment payment) {
        User user = payment.getUser();
        Long planId = payment.getPlan().getId();
        botClient.sendMessage(user.getMaxUserId(),
                "❌ Оплата не прошла.",
                List.of(List.of(
                        btn("🔄 Повторить", "action:retry_payment:" + planId),
                        btn("💬 Поддержка", "action:support")
                ))
        );
    }

    private void startSellerOnboarding(User user) {
        sessionService.save(user.getMaxUserId(),
                BotSessionService.State.ONBOARDING_INFO,
                new BotSessionService.Draft());

        botClient.sendMessage(user.getMaxUserId(),
                "💼 Для приёма платежей нужно быть самозанятым, ИП или ООО.\n" +
                "Зарегистрироваться: https://npd.nalog.ru",
                List.of(List.of(
                        btn("✅ Я оформлен", "action:selfemp:confirm"),
                        btn("📖 Подробнее", "action:support")
                ))
        );
    }

    private void handleSelfEmpConfirm(User user) {
        userService.confirmSelfEmployed(user.getMaxUserId());
        sessionService.save(user.getMaxUserId(),
                BotSessionService.State.ASK_TITLE,
                new BotSessionService.Draft());
        botClient.sendMessage(user.getMaxUserId(),
                "✅ Подтверждено!\n\n📝 Как называется ваш проект/сообщество? (1–255 символов)");
    }

    private void handleFsmText(User user, BotSessionService.State state,
                               BotSessionService.Draft draft, String text) {
        String maxUserId = user.getMaxUserId();
        switch (state) {
            case ASK_TITLE -> {
                if (text.length() < 1 || text.length() > 255) {
                    botClient.sendMessage(maxUserId, "❗ Название: 1–255 символов. Попробуй ещё раз:"); return;
                }
                draft.setTitle(text);
                sessionService.save(maxUserId, BotSessionService.State.ASK_DESCRIPTION, draft);
                botClient.sendMessage(maxUserId, "📄 Короткое описание (до 200 символов):");
            }
            case ASK_DESCRIPTION -> {
                if (text.length() > 200) {
                    botClient.sendMessage(maxUserId, "❗ Описание не более 200 символов:"); return;
                }
                draft.setDescription(text);
                sessionService.save(maxUserId, BotSessionService.State.ASK_CATEGORY, draft);
                botClient.sendMessage(maxUserId, "🏷 Выберите категорию:", categoryButtons());
            }
            case ASK_PLAN_TITLE -> {
                if (text.length() < 1 || text.length() > 128) {
                    botClient.sendMessage(maxUserId, "❗ Название тарифа: 1–128 символов:"); return;
                }
                draft.setPlanTitle(text);
                sessionService.save(maxUserId, BotSessionService.State.ASK_PLAN_PRICE, draft);
                botClient.sendMessage(maxUserId, "💰 Цена в рублях (например 990):");
            }
            case ASK_PLAN_PRICE -> {
                try {
                    BigDecimal price = new BigDecimal(text.trim().replace(",", "."));
                    if (price.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
                    draft.setPlanPrice(price);
                    sessionService.save(maxUserId, BotSessionService.State.ASK_PLAN_PERIOD, draft);
                    botClient.sendMessage(maxUserId, "📅 Период доступа:", periodButtons());
                } catch (NumberFormatException e) {
                    botClient.sendMessage(maxUserId, "❗ Введи число, например 990:");
                }
            }
            default -> botClient.sendMessage(maxUserId, "Не понял. Нажми /start");
        }
    }

    private void handleCategorySelected(User user, BotSessionService.Draft draft, String categoryKey) {
        String maxUserId = user.getMaxUserId();
        try {
            draft.setCategory(CommunityCategory.valueOf(categoryKey.toUpperCase()));
            sessionService.save(maxUserId, BotSessionService.State.ASK_PLAN_TITLE, draft);
            botClient.sendMessage(maxUserId, "✅ Категория выбрана!\n\n📝 Название тарифа:");
        } catch (IllegalArgumentException e) {
            botClient.sendMessage(maxUserId, "❗ Выберите категорию из списка:", categoryButtons());
        }
    }

    private void handlePeriodSelected(User user, BotSessionService.Draft draft, String periodStr) {
        String maxUserId = user.getMaxUserId();
        try {
            draft.setPlanPeriodDays(Integer.parseInt(periodStr));

            sessionService.save(maxUserId, BotSessionService.State.WAIT_BOT_ADDED, draft, null);
            botClient.sendMessage(maxUserId,
                    "⚙️ Осталось привязать канал:\n\n" +
                    "1. Добавьте бота администратором в ваш канал/группу\n" +
                    "2. Дайте право **Управлять участниками** (add_remove_members)\n\n" +
                    "Всё настроится автоматически.",
                    List.of(List.of(btn("⏳ Жду...", "action:menu")))
            );
        } catch (NumberFormatException e) {
            botClient.sendMessage(maxUserId, "❗ Выберите период:", periodButtons());
        }
    }

    private void handleStartPlan(User user, String planIdStr) {
        try {
            Long planId = Long.parseLong(planIdStr);
            botClient.sendMessage(user.getMaxUserId(), "🛍 Открываю страницу тарифа...",
                    List.of(List.of(btn("Открыть →", appBaseUrl + "?start=plan_" + planId))));
        } catch (NumberFormatException e) {
            botClient.sendMessage(user.getMaxUserId(), "❌ Тариф не найден.");
        }
    }

    private void handleMySubscriptions(User user) {
        List<Subscription> subs = subscriptionService.findMySubscriptions(user.getMaxUserId());
        if (subs.isEmpty()) {
            botClient.sendMessage(user.getMaxUserId(), "📋 Подписок пока нет.", mainMenuButtons());
            return;
        }
        StringBuilder sb = new StringBuilder("📋 *Мои подписки:*\n\n");
        List<List<MaxInlineButtonDto>> keyboard = new ArrayList<>();
        for (Subscription sub : subs) {
            String status = sub.getStatus() == SubscriptionStatus.ACTIVE ? "✅" : "❌";
            String daysLeft = "";
            if (sub.getExpiresAt() != null && sub.getStatus() == SubscriptionStatus.ACTIVE) {
                long d = java.time.Duration.between(Instant.now(), sub.getExpiresAt()).toDays();
                daysLeft = d > 0 ? " (" + d + " дн.)" : " (истекает сегодня)";
            }
            sb.append(status).append(" ").append(sub.getCommunity().getTitle()).append(daysLeft).append("\n");

            if (sub.getStatus() == SubscriptionStatus.ACTIVE) {
                keyboard.add(List.of(btn("🔗 Ссылка: " + sub.getCommunity().getTitle(),
                        "action:resend_invite:" + sub.getId())));
            } else {
                keyboard.add(List.of(btn("🔄 Продлить: " + sub.getCommunity().getTitle(),
                        "action:renew:" + sub.getId())));
            }
        }
        keyboard.add(List.of(btn("🏠 Меню", "action:menu")));
        botClient.sendMessage(user.getMaxUserId(), sb.toString(), keyboard);
    }

    private void handleResendInvite(User user, Long subscriptionId) {
        try {
            InviteToken token = subscriptionService.reissueToken(subscriptionId, user.getMaxUserId());
            String url = appBaseUrl + "/i/" + token.getToken();
            botClient.sendMessage(user.getMaxUserId(), "🔗 Ваша ссылка (24 ч):\n" + url,
                    List.of(List.of(btn("Вступить →", url))));
        } catch (Exception e) {
            botClient.sendMessage(user.getMaxUserId(), "❌ " + e.getMessage());
        }
    }

    private void handleRenew(User user, Long subscriptionId) {
        Subscription sub = subscriptionService.getById(subscriptionId);
        handleRetryPayment(user, sub.getPlan().getId());
    }

    private void handleRetryPayment(User user, Long planId) {
        try {
            CreatePaymentResponse resp = paymentService.createOrGetPending(user, planId);
            botClient.sendMessage(user.getMaxUserId(), "💳 Оплатите подписку:",
                    List.of(List.of(btn("Оплатить →", resp.getConfirmationUrl()))));
        } catch (IllegalStateException e) {
            botClient.sendMessage(user.getMaxUserId(),
                    "PLAN_INACTIVE".equals(e.getMessage())
                            ? "❌ Этот тариф больше не продаётся."
                            : "❌ Ошибка оплаты. Попробуй позже.");
        } catch (Exception e) {
            log.error("Payment retry failed userId={} planId={}: {}", user.getMaxUserId(), planId, e.getMessage(), e);
            botClient.sendMessage(user.getMaxUserId(), "❌ Не удалось создать платёж.");
        }
    }

    private void sendMainMenu(String maxUserId, String firstName) {
        String greeting = firstName != null ? "Привет, " + firstName + "! " : "";
        botClient.sendMessage(maxUserId, greeting + "🌸 Добро пожаловать в MAX Bloom!", mainMenuButtons());
    }

    private List<List<MaxInlineButtonDto>> mainMenuButtons() {
        return List.of(List.of(
                btn("💼 Кабинет продавца", "action:role:seller"),
                btn("🛍 Каталог", "action:catalog"),
                btn("📋 Мои подписки", "action:my_subs")
        ));
    }

    private List<List<MaxInlineButtonDto>> categoryButtons() {
        return Arrays.stream(CommunityCategory.values())
                .map(cat -> List.of(btn(cat.name(), "action:category:" + cat.name())))
                .toList();
    }

    private List<List<MaxInlineButtonDto>> periodButtons() {
        return List.of(List.of(
                btn("7 дней", "action:period:7"),
                btn("30 дней", "action:period:30"),
                btn("90 дней", "action:period:90"),
                btn("Разово", "action:period:0")
        ));
    }

    private MaxInlineButtonDto btn(String text, String payload) {
        if (payload.startsWith("http://") || payload.startsWith("https://")) {
            return MaxInlineButtonDto.link(text, payload);
        }
        return MaxInlineButtonDto.callback(text, payload);
    }
}
