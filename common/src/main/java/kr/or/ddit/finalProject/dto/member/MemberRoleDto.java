package kr.or.ddit.finalProject.dto.member;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberRoleDto implements Serializable {

    private String userRoleCd; // 기본키(PK)
    private String authrtNm;
    private String mdfrId; // 마지막 수정한 사용자 ID
    private LocalDateTime mdfcnDt; // 데이터 수정 시점
    private String rgtrId; // 처음 등록한 사용자 ID
    private LocalDateTime regDt; // 데이터 최초 생성 시점
    private String lastMdfrId; // 가장 최근 수정자 ID
}
