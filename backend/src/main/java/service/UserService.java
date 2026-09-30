package service;

import database.entity.User;
import database.repository.UserRepository;
import dto.bot.MaxUserDto;
import database.entity.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис управления пользователями платформы.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Поиск существующего или создание нового пользователя.
     */
    @Transactional
    public User findOrCreate(MaxUserDto dto) {
        String maxUserId = String.valueOf(dto.getUserId());
        return userRepository.findByMaxUserId(maxUserId)
                .map(existing -> {
                    boolean changed = false;
                    if (dto.getUsername() != null && !dto.getUsername().equals(existing.getUsername())) {
                        existing.setUsername(dto.getUsername());
                        changed = true;
                    }
                    if (dto.getFirstName() != null && !dto.getFirstName().equals(existing.getFirstName())) {
                        existing.setFirstName(dto.getFirstName());
                        changed = true;
                    }
                    if (changed) {
                        log.debug("Updated user profile maxUserId={}", maxUserId);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    User user = User.builder()
                            .maxUserId(maxUserId)
                            .username(dto.getUsername())
                            .firstName(dto.getFirstName())
                            .pdpConsentGiven(true)
                            .role(UserRole.USER)
                            .build();
                    User saved = userRepository.save(user);
                    log.info("Registered new user maxUserId={}", maxUserId);
                    return saved;
                });
    }

    /**
     * Подтверждение статуса самозанятого и присвоение роли BUSINESS.
     */
    @Transactional
    public User confirmSelfEmployed(String maxUserId) {
        User user = userRepository.findByMaxUserId(maxUserId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + maxUserId));
        user.setSelfEmployedConfirmed(true);
        user.setRole(UserRole.BUSINESS);
        log.info("User {} confirmed self-employment, role -> BUSINESS", maxUserId);
        return user;
    }

    /**
     * Получение пользователя по его идентификатору MAX.
     */
    @Transactional(readOnly = true)
    public User getByMaxUserId(String maxUserId) {
        return userRepository.findByMaxUserId(maxUserId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + maxUserId));
    }

    @Transactional
    public User getOrCreateByMaxUserId(String maxUserId) {
        return userRepository.findByMaxUserId(maxUserId)
                .orElseGet(() -> userRepository.save(User.builder()
                        .maxUserId(maxUserId)
                        .username("user_" + maxUserId)
                        .firstName("User")
                        .pdpConsentGiven(true)
                        .role(UserRole.USER)
                        .build()));
    }
}
