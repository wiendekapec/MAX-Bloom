package dto.plan;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Запрос на включение/выключение тарифа (is_active).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TogglePlanRequest {

    @NotNull(message = "Флаг активности тарифа обязателен")
    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonSetter("active")
    public void setActive(Boolean active) {
        this.isActive = active;
    }

    @JsonSetter("isActive")
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @JsonProperty("isActive")
    public Boolean getIsActive() {
        return isActive;
    }
}

