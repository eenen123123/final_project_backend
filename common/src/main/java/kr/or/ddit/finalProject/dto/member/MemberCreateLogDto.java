package kr.or.ddit.finalProject.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberCreateLogDto {
    private Integer logId;
    private String userId;
    private String rgtrId;
    private String lastMdfrId;
    private String regDt;
    private String mdfcnDt;
}
