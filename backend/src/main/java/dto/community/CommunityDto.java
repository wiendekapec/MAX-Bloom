package dto.community;

import com.fasterxml.jackson.annotation.JsonInclude;
import dto.plan.SubscriptionPlanDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Сообщество (канал/чат) автора.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommunityDto {
    private Long id;
    private Long creatorId;
    private String title;
    private String description;
    private CommunityCategory category;
    private String avatarUrl;
    private Integer subscribersCount;
    private List<SubscriptionPlanDto> plans;
    private Boolean isDemo;
}
