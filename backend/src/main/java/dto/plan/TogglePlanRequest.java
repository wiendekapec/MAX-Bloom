package dto.plan;

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
    private Boolean isActive;
}

