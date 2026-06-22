package kr.or.ddit.finalProject.dto.classroom;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 클래스룸 수강생 목록 응답 DTO.
 * 수강생 기본 정보 및 강의 진도율을 담는다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomMemberListResponse {

    private String userId;          // 수강생 아이디
    private String userName;        // 수강생 이름
    private EnrollStatus enrlStatCd; // 수강 상태 (ENROLLED: 수강중, COMPLETED: 이수완료, WITHDRAWN: 중도탈퇴, CANCELLED: 등록취소)
    private LocalDateTime regDt;    // 수강 등록일시 — MyBatis 매핑용 원본값
    private String formattedRegDt;  // 화면 표시용 등록일시 (yyyy-MM-dd) — 서비스에서 포맷팅 후 세팅
    private double progressRate;    // 개인 강의 진도율 (0~100, 정수) — 서비스에서 별도 쿼리로 병합

}
