package api;

import database.entity.Community;
import database.entity.InviteToken;
import database.entity.Subscription;
import database.entity.SubscriptionPlan;
import database.repository.CommunityRepository;
import database.repository.SubscriptionPlanRepository;
import database.repository.SubscriptionRepository;
import dto.community.CommunityDto;
import dto.plan.SubscriptionPlanDto;
import dto.subscription.ReissueInviteResponse;
import dto.subscription.SubscriptionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.CommunityService;
import service.MaxInitDataVerifier;
import service.SubscriptionService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST-контроллер каталога сообществ, тарифов и подписок пользователя.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityRepository communityRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CommunityService communityService;
    private final SubscriptionService subscriptionService;
    private final MaxInitDataVerifier initDataVerifier;

    @Value("${app.base-url:http://localhost:80}")
    private String appBaseUrl;

    /**
     * Получение списка всех доступных сообществ.
     */
    @GetMapping("/communities")
    public ResponseEntity<List<CommunityDto>> getCommunities() {
        List<Community> communities = communityRepository.findAll();
        List<CommunityDto> dtos = communities.stream().map(this::toCommunityDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Получение сообщества по идентификатору с активными тарифами.
     */
    @GetMapping("/communities/{id}")
    public ResponseEntity<CommunityDto> getCommunity(@PathVariable Long id) {
        Community community = communityService.getById(id);
        return ResponseEntity.ok(toCommunityDto(community));
    }

    /**
     * Получение тарифного плана по идентификатору.
     */
    @GetMapping("/plans/{id}")
    public ResponseEntity<SubscriptionPlanDto> getPlan(@PathVariable Long id) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Тариф не найден: " + id));
        return ResponseEntity.ok(toPlanDto(plan));
    }

    /**
     * Получение активных и архивных подписок текущего пользователя.
     */
    @GetMapping("/subscriptions/my")
    public ResponseEntity<List<SubscriptionDto>> getMySubscriptions(
            @RequestHeader("X-Init-Data") String initData
    ) {
        String maxUserId = initDataVerifier.verifyAndExtractUserId(initData);
        List<Subscription> subs = subscriptionRepository.findByUserMaxUserId(maxUserId);

        Instant now = Instant.now();
        List<SubscriptionDto> dtos = subs.stream().map(s -> {
            Integer daysLeft = null;
            if (s.getExpiresAt() != null) {
                long days = ChronoUnit.DAYS.between(now, s.getExpiresAt());
                daysLeft = (int) Math.max(0, days);
            }

            return SubscriptionDto.builder()
                    .id(s.getId())
                    .communityId(s.getCommunity().getId())
                    .communityTitle(s.getCommunity().getTitle())
                    .planId(s.getPlan().getId())
                    .planTitle(s.getPlan().getTitle())
                    .priceRub(s.getPlan().getPriceRub())
                    .status(s.getStatus())
                    .startsAt(s.getStartsAt())
                    .expiresAt(s.getExpiresAt())
                    .daysLeft(daysLeft)
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Перевыпуск инвайт-ссылки для подписки.
     */
    @PostMapping("/subscriptions/{id}/invite")
    public ResponseEntity<ReissueInviteResponse> reissueInvite(
            @RequestHeader("X-Init-Data") String initData,
            @PathVariable Long id
    ) {
        String maxUserId = initDataVerifier.verifyAndExtractUserId(initData);
        InviteToken token = subscriptionService.reissueToken(id, maxUserId);
        return ResponseEntity.ok(new ReissueInviteResponse(appBaseUrl + "/i/" + token.getToken()));
    }

    private CommunityDto toCommunityDto(Community c) {
        List<SubscriptionPlanDto> planDtos = planRepository.findByCommunityId(c.getId()).stream()
                .filter(SubscriptionPlan::getIsActive)
                .map(this::toPlanDto)
                .collect(Collectors.toList());

        return CommunityDto.builder()
                .id(c.getId())
                .creatorId(c.getCreator().getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .category(c.getCategory())
                .avatarUrl(c.getAvatarUrl())
                .subscribersCount(c.getSubscribersCount())
                .plans(planDtos)
                .isDemo(c.getIsDemo())
                .build();
    }

    private SubscriptionPlanDto toPlanDto(SubscriptionPlan plan) {
        return SubscriptionPlanDto.builder()
                .id(plan.getId())
                .title(plan.getTitle())
                .description(plan.getDescription())
                .priceRub(plan.getPriceRub())
                .periodDays(plan.getPeriodDays())
                .isActive(plan.getIsActive())
                .build();
    }
}
