package dto.bot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Информация об участнике чата/канала (и правах бота) в MAX.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaxChatMemberDto {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("can_manage_members")
    private Boolean canManageMembers;

    @JsonProperty("add_remove_members")
    private Boolean addRemoveMembers;

    /**
     * Проверка наличия прав на исключение участников.
     */
    public boolean hasKickPermission() {
        return Boolean.TRUE.equals(canManageMembers) || Boolean.TRUE.equals(addRemoveMembers);
    }
}
