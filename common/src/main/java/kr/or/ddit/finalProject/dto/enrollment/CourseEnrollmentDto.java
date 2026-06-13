package kr.or.ddit.finalProject.dto.enrollment;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseEnrollmentDto {

    private Long enrlSn; // PK
    private String userId; // FK → MEMBER.USER_ID
    private Long courseSn; // FK → COURSE.COURSE_SN
    private Long ordSn; // FK → ORDERS.ORD_SN (권한 부여/연장한 마지막 주문)
    private LocalDateTime accsStrtDt; // 이용 시작일
    private LocalDateTime accsEndDt; // 이용 만기일 (시작일 + 1년)
    private EnrollmentStatus enrlStatCd; // ACTIVE / REVOKED
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;

    // ── COURSE JOIN 필드 (수강 목록 조회 시 사용) ─────────────────────────────
    private String courseNm; // COURSE.COURSE_NM
    private String thmbImg; // COURSE.THMB_IMG
    private String instrNm; // MEMBER.USER_NAME (강사명)
    private int progressPct; // 진척도

}
