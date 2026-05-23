package kr.or.ddit.finalProject.dto.user;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentJoinLinkDto implements Serializable {

    private Long linkId; // 시퀀스
    private String stdUserId; // STUDENT.USER_ID 참조 (FK)
    private String joinLinkAddr; // 학부모 가입 초대 URL
    private LocalDateTime linkCretDt;
    private LocalDateTime linkExprDt; // 링크 유효기간 만료 일시
    private String linkStatCd; // COM_CD 공통코드 참조 권장
}