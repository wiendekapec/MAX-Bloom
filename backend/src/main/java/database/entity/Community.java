package database.entity;

import dto.community.CommunityCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Сообщество (канал или чат в платформе MAX).
 */
@Entity
@Table(name = "communities", indexes = {
    @Index(name = "idx_communities_max_chat_id", columnList = "max_chat_id", unique = true),
    @Index(name = "idx_communities_creator_id", columnList = "creator_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(name = "max_chat_id", nullable = false, unique = true, length = 64)
    private String maxChatId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 64)
    private CommunityCategory category;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    /**
     * Ссылка на канал в платформе MAX.
     */
    @Column(name = "invite_link", columnDefinition = "TEXT")
    private String inviteLink;

    @Column(name = "subscribers_count", nullable = false)
    @Builder.Default
    private Integer subscribersCount = 0;

    /**
     * Флаг демонстрационного сообщества.
     */
    @Column(name = "is_demo", nullable = false)
    @Builder.Default
    private Boolean isDemo = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SubscriptionPlan> plans = new ArrayList<>();
}
