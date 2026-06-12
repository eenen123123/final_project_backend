package kr.or.ddit.finalProject.dto.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberActivityLogDto {
    private Long activityId;
    private String traceId;
    private String userId;
    private String activityType;
    private String targetId;
    private String activityIp;
    private Integer statusCode;
    private String createdAt;
}
