package database.entity;

/**
 * Статус одноразового инвайт-токена.
 */
public enum InviteTokenStatus {
    ACTIVE,
    USED,
    EXPIRED,
    REVOKED
}
