package dto.community;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Категории сообществ в каталоге MAX Bloom.
 */
public enum CommunityCategory {
    BUSINESS("business"),
    EDUCATION("education"),
    FITNESS("fitness"),
    TECH("tech"),
    SERVICES("services");

    private final String value;

    CommunityCategory(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static CommunityCategory fromValue(String text) {
        for (CommunityCategory category : CommunityCategory.values()) {
            if (category.value.equalsIgnoreCase(text)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + text);
    }
}
