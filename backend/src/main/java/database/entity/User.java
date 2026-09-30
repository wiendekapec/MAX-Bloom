package database.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
     * Подтверждение статуса самозанятого.
     */
    @Column(name = "self_employed_confirmed", nullable = false)
    @Builder.Default
    private Boolean selfEmployedConfirmed = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
