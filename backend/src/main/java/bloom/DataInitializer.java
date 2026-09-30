package bloom;

import database.entity.Community;
import database.entity.SubscriptionPlan;
import database.entity.User;
import database.entity.UserRole;
import database.repository.CommunityRepository;
import database.repository.SubscriptionPlanRepository;
import database.repository.UserRepository;
import dto.community.CommunityCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final SubscriptionPlanRepository planRepository;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            if (communityRepository.count() > 0) {
                return;
            }

            log.info("Seeding initial demo communities and subscription plans...");

            User devUser = userRepository.findByMaxUserId("12345678")
                    .orElseGet(() -> userRepository.save(User.builder()
                            .maxUserId("12345678")
                            .username("devuser")
                            .firstName("Dev")
                            .role(UserRole.BUSINESS)
                            .pdpConsentGiven(true)
                            .selfEmployedConfirmed(true)
                            .build()));

            createCommunityWithPlans(
                    devUser,
                    "chat_mentor_club",
                    "IT Mentor Club",
                    "Разборы задач, код-ревью и закрытые воркшопы для разработчиков. Практика, а не теория.",
                    CommunityCategory.TECH,
                    214,
                    List.of(
                            new PlanSpec("Базовый", "Доступ ко всем материалам", new BigDecimal("990.00"), 30),
                            new PlanSpec("Pro с менторством", "+ 1 созвон-разбор в месяц", new BigDecimal("2990.00"), 30)
                    )
            );

            createCommunityWithPlans(
                    devUser,
                    "chat_school_exam",
                    "Школа ЕГЭ/ОГЭ",
                    "Репетиторство по математике, физике и информатике. Гарантия повышения балла или возврат.",
                    CommunityCategory.EDUCATION,
                    87,
                    List.of(
                            new PlanSpec("Математика", "Разборы + задачники", new BigDecimal("1490.00"), 30),
                            new PlanSpec("Всё включено", "Все предметы + чат с преподавателем", new BigDecimal("3490.00"), 30)
                    )
            );

            createCommunityWithPlans(
                    devUser,
                    "chat_fitness_home",
                    "Фитнес Дома",
                    "Онлайн-тренировки без оборудования. 20 минут в день — реальный результат за месяц.",
                    CommunityCategory.FITNESS,
                    456,
                    List.of(
                            new PlanSpec("Старт", "3 тренировки в неделю", new BigDecimal("690.00"), 30),
                            new PlanSpec("Интенсив", "Ежедневные тренировки + питание", new BigDecimal("1290.00"), 30)
                    )
            );

            createCommunityWithPlans(
                    devUser,
                    "chat_lawyer_ip",
                    "Юрист для ИП",
                    "Консультации по налогам, договорам и спорам с контрагентами. Отвечаю за 2 часа.",
                    CommunityCategory.SERVICES,
                    63,
                    List.of(
                            new PlanSpec("Разовая консультация", "60 минут + документ", new BigDecimal("2500.00"), 0),
                            new PlanSpec("Абонемент", "До 5 консультаций в месяц", new BigDecimal("8900.00"), 30)
                    )
            );

            createCommunityWithPlans(
                    devUser,
                    "chat_business_breakfast",
                    "Бизнес-Завтраки",
                    "Закрытый клуб предпринимателей. Еженедельные встречи, нетворкинг, совместные проекты.",
                    CommunityCategory.BUSINESS,
                    41,
                    List.of(
                            new PlanSpec("Участник", "Встречи + чат", new BigDecimal("4900.00"), 30),
                            new PlanSpec("VIP", "+ менторинг и партнёрства", new BigDecimal("14900.00"), 30)
                    )
            );

            log.info("Demo data seeding completed successfully.");
        } catch (Exception e) {
            log.error("Failed to seed demo data: {}", e.getMessage(), e);
        }
    }

    private void createCommunityWithPlans(User creator, String chatId, String title, String desc,
                                          CommunityCategory cat, int subs, List<PlanSpec> plans) {
        Community community = communityRepository.save(Community.builder()
                .creator(creator)
                .maxChatId(chatId)
                .title(title)
                .description(desc)
                .category(cat)
                .subscribersCount(subs)
                .isDemo(true)
                .plans(new ArrayList<>())
                .build());

        for (PlanSpec ps : plans) {
            planRepository.save(SubscriptionPlan.builder()
                    .community(community)
                    .title(ps.title)
                    .description(ps.desc)
                    .price(ps.price)
                    .periodDays(ps.periodDays)
                    .isActive(true)
                    .build());
        }
    }

    private record PlanSpec(String title, String desc, BigDecimal price, int periodDays) {}
}
