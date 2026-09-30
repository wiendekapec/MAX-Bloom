package database.repository;

import database.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий тарифных планов подписки.
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    List<SubscriptionPlan> findByCommunityIdAndIsActiveTrue(Long communityId);

    List<SubscriptionPlan> findByCommunityId(Long communityId);

    long countByCommunityId(Long communityId);

    long countByCommunityIdAndIsActiveTrue(Long communityId);
}
