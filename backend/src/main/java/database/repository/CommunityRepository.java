package database.repository;

import database.entity.Community;
import dto.community.CommunityCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {

    Optional<Community> findByMaxChatId(String maxChatId);

    boolean existsByMaxChatId(String maxChatId);

    List<Community> findByCreatorId(Long creatorId);

    List<Community> findByCreatorMaxUserId(String maxUserId);

    List<Community> findByIsDemoTrue();

    List<Community> findByCategory(CommunityCategory category);
}
