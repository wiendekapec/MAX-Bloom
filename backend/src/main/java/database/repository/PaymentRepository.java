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

/**
 * Репозиторий платежей и финансовых транзакций.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdempotencyKey(UUID idempotencyKey);

    Optional<Payment> findByYukassaPaymentId(String yukassaPaymentId);

    /**
     * Пессимистическая блокировка строки платежа для атомарной обработки вебхуков.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.yukassaPaymentId = :yukassaPaymentId")
    Optional<Payment> findByYukassaPaymentIdWithLock(@Param("yukassaPaymentId") String yukassaPaymentId);

    /**
     * Атомарное обновление статуса по принципу CAS.
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
     * Расчет суммарной выручки сообщества по статусу платежа.
     */
    @Query("SELECT sum(p.amountRub) FROM Payment p " +
           "WHERE p.plan.community.id = :communityId AND p.status = :status")
    BigDecimal calculateRevenueByCommunityIdAndStatus(
            @Param("communityId") Long communityId,
            @Param("status") PaymentStatus status);

    /**
     * Расчет выручки сообщества с заданной даты.
     */
    @Query("SELECT sum(p.amountRub) FROM Payment p " +
           "WHERE p.plan.community.id = :communityId AND p.status = :status AND p.createdAt >= :since")
    BigDecimal calculateRevenueSince(
            @Param("communityId") Long communityId,
            @Param("status") PaymentStatus status,
            @Param("since") Instant since);

    /**
     * Поиск незавершенного платежа пользователя по тарифу.
     */
    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.plan.id = :planId AND p.status = dto.payment.PaymentStatus.PENDING")
    Optional<Payment> findFirstPendingByUserAndPlan(
            @Param("userId") Long userId,
            @Param("planId") Long planId);
}
