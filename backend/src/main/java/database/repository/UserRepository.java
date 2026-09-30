package database.repository;

import database.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByMaxUserId(String maxUserId);

    boolean existsByMaxUserId(String maxUserId);

    Optional<User> findByUsername(String username);
}
