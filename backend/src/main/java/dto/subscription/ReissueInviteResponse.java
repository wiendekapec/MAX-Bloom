package dto.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ответ на перевыпуск инвайт-ссылки.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReissueInviteResponse {
    private String inviteUrl;
}
