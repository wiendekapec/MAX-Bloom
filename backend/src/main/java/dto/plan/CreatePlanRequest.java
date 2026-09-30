package dto.plan;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Запрос на создание нового тарифного плана.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlanRequest {

    @NotBlank(message = "Название тарифа не может быть пустым")
    @Size(min = 2, max = 128, message = "Название тарифа должно содержать от 2 до 128 символов")
    private String title;

    @Size(max = 500, message = "Описание тарифа не может превышать 500 символов")
    private String description;

    @NotNull(message = "Цена тарифа обязательна")
    @DecimalMin(value = "1.00", message = "Минимальная стоимость тарифа — 1 рубль")
    @DecimalMax(value = "500000.00", message = "Максимальная стоимость тарифа — 500 000 рублей")
    private BigDecimal priceRub;

    /**
     * Период доступа в днях.
     */
    @NotNull(message = "Период доступа обязателен")
    @Min(value = 0, message = "Период не может быть отрицательным")
    @Max(value = 365, message = "Максимальный период доступа — 365 дней")
    private Integer periodDays;

    /**
     * Подтверждение статуса самозанятого.
     */
    private Boolean selfEmployedConfirmed;
}

