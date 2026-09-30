package database.repository;

import database.entity.Payment;
import dto.payment.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdempotencyKey(UUID idempotencyKey);

    Optional<Payment> findByYukassaPaymentId(String yukassaPaymentId);

    /**
     * Пессимистическая блокировка строки платежа (SELECT ... FOR UPDATE) для атомарной обработки вебхуков.
     * Защищает от состояния гонки при параллельной доставке событий.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.yukassaPaymentId = :yukassaPaymentId")
    Optional<Payment> findByYukassaPaymentIdWithLock(@Param("yukassaPaymentId") String yukassaPaymentId);

    /**
     * Атомарное обновление статуса по принципу Compare-And-Swap (CAS).
     * Возвращает 1, только если текущий статус соответствовал ожидаемому.
     */
    @Modifying
    @Query("UPDATE Payment p SET p.status = :newStatus WHERE p.yukassaPaymentId = :yukassaPaymentId AND p.status = :expectedStatus")
    int updateStatusIfExpected(
            @Param("yukassaPaymentId") String yukassaPaymentId,
            @Param("newStatus") PaymentStatus newStatus,
            @Param("expectedStatus") PaymentStatus expectedStatus);

    List<Payment> findByUserId(Long userId);

    List<Payment> findByUserMaxUserIdOrderByCreatedAtDesc(String maxUserId);

    List<Payment> findByPlanCommunityIdOrderByCreatedAtDesc(Long communityId);

    long countByPlanId(Long planId);

    /**
     * Расчёт суммарной выручки сообщества по успешным платежам.
     */
    @Query("SELECT coalesce(sum(p.amountRub), 0) FROM Payment p " +
           "WHERE p.plan.community.id = :communityId AND p.status = :status")
    BigDecimal calculateRevenueByCommunityIdAndStatus(
            @Param("communityId") Long communityId,
            @Param("status") PaymentStatus status);

    /**
     * Расчёт выручки сообщества за период (например, за последние 30 дней) для дашборда.
     */
    @Query("SELECT coalesce(sum(p.amountRub), 0) FROM Payment p " +
           "WHERE p.plan.community.id = :communityId AND p.status = :status AND p.createdAt >= :since")
    BigDecimal calculateRevenueSince(
            @Param("communityId") Long communityId,
            @Param("status") PaymentStatus status,
            @Param("since") Instant since);

    /**
     * Проверить наличие PENDING-платежа пользователя на тариф (для идемпотентности создания).
     */
    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.plan.id = :planId AND p.status = 'PENDING'")
    Optional<Payment> findFirstPendingByUserAndPlan(
            @Param("userId") Long userId,
            @Param("planId") Long planId);
}
