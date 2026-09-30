package service;

import database.entity.Community;
import database.entity.SubscriptionPlan;
import database.entity.User;
import database.repository.CommunityRepository;
import database.repository.SubscriptionPlanRepository;
import dto.community.CommunityCategory;
import dto.community.RegisterBusinessRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис управления сообществами.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final SubscriptionPlanRepository planRepository;

    /**
     * Поиск сообщества по идентификатору.
     */
    @Transactional(readOnly = true)
    public Community getById(Long id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Сообщество не найдено: " + id));
    }

    /**
     * Поиск сообществ по создателю.
     */
    @Transactional(readOnly = true)
    public List<Community> findByCreatorMaxUserId(String maxUserId) {
        return communityRepository.findByCreatorMaxUserId(maxUserId);
    }

    /**
     * Обновление ссылки-приглашения сообщества.
     */
    @Transactional
    public Community updateInviteLink(Long communityId, String inviteLink) {
        Community community = getById(communityId);
        community.setInviteLink(inviteLink);
        log.debug("Updated inviteLink communityId={}", communityId);
        return community;
    }

    /**
     * Обновление количества подписчиков.
     */
    @Transactional
    public Community updateSubscribersCount(Long communityId, int count) {
        Community community = getById(communityId);
        community.setSubscribersCount(count);
        return community;
    }

    /**
     * Регистрация сообщества через мини-апп.
     */
    @Transactional
    public Community registerCommunity(User creator, RegisterBusinessRequest request) {
        String tempChatId = "temp_" + creator.getMaxUserId() + "_" + System.currentTimeMillis();
        Community community = Community.builder()
                .creator(creator)
                .maxChatId(tempChatId)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .subscribersCount(0)
                .isDemo(false)
                .plans(new ArrayList<>())
                .build();
        Community saved = communityRepository.save(community);
        log.info("Registered community id={} creator={}", saved.getId(), creator.getMaxUserId());
        return saved;
    }

    /**
     * Финализация сообщества и создание тарифного плана после добавления бота в чат.
     */
    @Transactional
    public Community finalizeCommunity(
            User creator,
            String maxChatId,
            String title,
            String description,
            CommunityCategory category,
            String inviteLink,
            String planTitle,
            String planDescription,
            BigDecimal planPrice,
            Integer planPeriodDays
    ) {
        Community community = communityRepository.findByMaxChatId(maxChatId)
                .orElse(Community.builder()
                        .creator(creator)
                        .maxChatId(maxChatId)
                        .title(title)
                        .description(description)
                        .category(category)
                        .inviteLink(inviteLink)
                        .subscribersCount(0)
                        .isDemo(false)
                        .plans(new ArrayList<>())
                        .build());

        community.setTitle(title);
        community.setDescription(description);
        community.setCategory(category);
        community.setInviteLink(inviteLink);
        Community savedCommunity = communityRepository.save(community);

        if (planTitle != null && !planTitle.isBlank() && planPrice != null) {
            SubscriptionPlan plan = SubscriptionPlan.builder()
                    .community(savedCommunity)
                    .title(planTitle)
                    .description(planDescription)
                    .price(planPrice)
                    .periodDays(planPeriodDays != null ? planPeriodDays : 30)
                    .isActive(true)
                    .build();
            SubscriptionPlan savedPlan = planRepository.save(plan);
            savedCommunity.getPlans().add(savedPlan);
        }

        log.info("Finalized community id={} chatId={}", savedCommunity.getId(), maxChatId);
        return savedCommunity;
    }
}
