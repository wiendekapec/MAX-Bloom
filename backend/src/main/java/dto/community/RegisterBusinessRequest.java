package dto.community;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Запрос на регистрацию сообщества автором.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterBusinessRequest {

    @NotBlank(message = "Название сообщества обязательно")
    @Size(min = 2, max = 128, message = "Название сообщества должно содержать от 2 до 128 символов")
    private String title;

    @Size(max = 500, message = "Описание сообщества не должно превышать 500 символов")
    private String description;

    @NotNull(message = "Категория сообщества обязательна")
    private CommunityCategory category;
}

