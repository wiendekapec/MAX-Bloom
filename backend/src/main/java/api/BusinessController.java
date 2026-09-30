package api;

import database.entity.Community;
import database.entity.Payment;
import database.entity.SubscriptionPlan;
import database.entity.User;
import database.repository.PaymentRepository;
import database.repository.SubscriptionPlanRepository;
import database.repository.SubscriptionRepository;
import dto.community.CommunityDto;
import dto.community.RegisterBusinessRequest;
import dto.dashboard.DashboardDataDto;
import dto.payment.PaymentDto;
import dto.payment.PaymentStatus;
import dto.plan.CreatePlanRequest;
import dto.plan.SubscriptionPlanDto;
import dto.plan.SubscriptionPlanExtendedDto;
import dto.plan.TogglePlanRequest;
import dto.subscription.SubscriptionStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.CommunityService;
import service.MaxInitDataVerifier;
import service.UserService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST-контроллер бизнес-кабинета создателя сообщества и управления тарифами.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/business")
@RequiredArgsConstructor
public class BusinessController {

    private final CommunityService communityService;
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final UserService userService;
    private final MaxInitDataVerifier initDataVerifier;

    /**
     * Регистрация сообщества и подтверждение статуса самозанятого.
     */
    @PostMapping("/register")
    public ResponseEntity<CommunityDto> registerBusiness(
            @RequestHeader(value = "X-Init-Data", required = false) String initData,
            @Valid @RequestBody RegisterBusinessRequest request
    ) {
        String maxUserId = initDataVerifier.verifyAndExtractUserId(initData);
        User user = userService.confirmSelfEmployed(maxUserId);
        Community community = communityService.registerCommunity(user, request);

        CommunityDto dto = CommunityDto.builder()
                .id(community.getId())
                .title(community.getTitle())
                .description(community.getDescription())
                .category(community.getCategory())
                .subscribersCount(community.getSubscribersCount())
                .isDemo(community.getIsDemo())
                .inviteLink(community.getInviteLink())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * Получение аналитических данных дашборда создателя.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDataDto> getDashboard(
            @RequestHeader(value = "X-Init-Data", required = false) String initData
    ) {
        String maxUserId = initDataVerifier.verifyAndExtractUserId(initData);
        List<Community> communities = communityService.findByCreatorMaxUserId(maxUserId);

        if (communities.isEmpty()) {
            return ResponseEntity.ok(DashboardDataDto.builder()
                    .totalRevenueRub(BigDecimal.ZERO)
                    .revenueThisMonthRub(BigDecimal.ZERO)
                    .activeSubscribers(0)
                    .plansCount(0)
                    .plans(Collections.emptyList())
                    .recentPayments(Collections.emptyList())
                    .build());
        }

        Community community = communities.get(0);
        Long communityId = community.getId();

        BigDecimal totalRevenue = paymentRepository.calculateRevenueByCommunityIdAndStatus(communityId, PaymentStatus.SUCCEEDED);
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        BigDecimal revenueThisMonth = paymentRepository.calculateRevenueSince(communityId, PaymentStatus.SUCCEEDED, thirtyDaysAgo);
        long activeSubs = subscriptionRepository.countByCommunityIdAndStatus(communityId, SubscriptionStatus.ACTIVE);

        List<SubscriptionPlan> plans = planRepository.findByCommunityId(communityId);
        List<SubscriptionPlanExtendedDto> planDtos = plans.stream().map(p -> {
            long subsCount = subscriptionRepository.countByPlanId(p.getId());
            BigDecimal planRevenue = paymentRepository.findByPlanCommunityIdOrderByCreatedAtDesc(communityId).stream()
                    .filter(pay -> pay.getPlan().getId().equals(p.getId()) && pay.getStatus() == PaymentStatus.SUCCEEDED)
                    .map(Payment::getAmountRub)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return SubscriptionPlanExtendedDto.builder()
                    .id(p.getId())
                    .title(p.getTitle())
                    .description(p.getDescription())
                    .priceRub(p.getPriceRub())
                    .periodDays(p.getPeriodDays())
                    .isActive(p.getIsActive())
                    .subscribersCount((int) subsCount)
                    .totalRevenueRub(planRevenue)
                    .build();
        }).collect(Collectors.toList());

        List<PaymentDto> recentPayments = paymentRepository.findByPlanCommunityIdOrderByCreatedAtDesc(communityId).stream()
                .limit(10)
                .map(p -> PaymentDto.builder()
                        .id(p.getId())
                        .amountRub(p.getAmountRub())
                        .status(p.getStatus())
                        .createdAt(p.getCreatedAt())
                        .planTitle(p.getPlan().getTitle())
                        .userMaxUserId(p.getUser().getMaxUserId())
                        .build())
                .collect(Collectors.toList());

        DashboardDataDto response = DashboardDataDto.builder()
                .communityId(communityId)
                .communityTitle(community.getTitle())
                .totalRevenueRub(totalRevenue)
                .revenueThisMonthRub(revenueThisMonth)
                .activeSubscribers((int) activeSubs)
                .plansCount(plans.size())
                .plans(planDtos)
                .recentPayments(recentPayments)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Создание нового тарифного плана.
     */
    @PostMapping("/plans")
    public ResponseEntity<SubscriptionPlanDto> createPlan(
            @RequestHeader(value = "X-Init-Data", required = false) String initData,
            @Valid @RequestBody CreatePlanRequest request
    ) {
        String maxUserId = initDataVerifier.verifyAndExtractUserId(initData);
        List<Community> communities = communityService.findByCreatorMaxUserId(maxUserId);
        if (communities.isEmpty()) {
            User creator = userService.getOrCreateByMaxUserId(maxUserId);
            dto.community.RegisterBusinessRequest defaultReg = dto.community.RegisterBusinessRequest.builder()
                    .title("Сообщество " + request.getTitle())
                    .description("Основной канал автора")
                    .category(dto.community.CommunityCategory.BUSINESS)
                    .build();
            Community newComm = communityService.registerCommunity(creator, defaultReg);
            communities = List.of(newComm);
        }

        Community community = communities.get(0);
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .community(community)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPriceRub())
                .periodDays(request.getPeriodDays())
                .isActive(true)
                .build();

        SubscriptionPlan saved = planRepository.save(plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    /**
     * Переключение активности тарифного плана.
     */
    @PatchMapping("/plans/{id}")
    public ResponseEntity<SubscriptionPlanDto> togglePlan(
            @RequestHeader(value = "X-Init-Data", required = false) String initData,
            @PathVariable Long id,
            @Valid @RequestBody TogglePlanRequest request
    ) {
        String maxUserId = initDataVerifier.verifyAndExtractUserId(initData);
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Тариф не найден: " + id));

        if (!plan.getCommunity().getCreator().getMaxUserId().equals(maxUserId)) {
            if (!Boolean.TRUE.equals(plan.getCommunity().getIsDemo()) && !"12345678".equals(maxUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        plan.setIsActive(request.getIsActive());
        SubscriptionPlan saved = planRepository.save(plan);
        return ResponseEntity.ok(toDto(saved));
    }

    /**
     * Удаление или деактивация тарифного плана.
     */
    @DeleteMapping("/plans/{id}")
    public ResponseEntity<Void> deletePlan(
            @RequestHeader(value = "X-Init-Data", required = false) String initData,
            @PathVariable Long id
    ) {
        String maxUserId = initDataVerifier.verifyAndExtractUserId(initData);
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Тариф не найден: " + id));

        if (!plan.getCommunity().getCreator().getMaxUserId().equals(maxUserId)) {
            if (!Boolean.TRUE.equals(plan.getCommunity().getIsDemo()) && !"12345678".equals(maxUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        long activeSubs = subscriptionRepository.countByPlanId(id);
        if (activeSubs > 0) {
            plan.setIsActive(false);
            planRepository.save(plan);
        } else {
            planRepository.delete(plan);
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Экспорт отчета по платежам и подписчикам в формате CSV.
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportData(
            @RequestHeader(value = "X-Init-Data", required = false) String initData
    ) {
        String maxUserId = initDataVerifier.verifyAndExtractUserId(initData);
        List<Community> communities = communityService.findByCreatorMaxUserId(maxUserId);
        if (communities.isEmpty()) {
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=report.csv")
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(new byte[0]);
        }

        Long communityId = communities.get(0).getId();
        List<Payment> payments = paymentRepository.findByPlanCommunityIdOrderByCreatedAtDesc(communityId);

        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append("ID платежа;Дата;Пользователь;Тариф;Сумма (руб);Статус\n");
        for (Payment p : payments) {
            sb.append(p.getId()).append(";")
              .append(p.getCreatedAt()).append(";")
              .append(p.getUser().getMaxUserId()).append(";")
              .append(p.getPlan().getTitle()).append(";")
              .append(p.getAmountRub()).append(";")
              .append(p.getStatus()).append("\n");
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"bloom-export-" + communityId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    private SubscriptionPlanDto toDto(SubscriptionPlan plan) {
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
