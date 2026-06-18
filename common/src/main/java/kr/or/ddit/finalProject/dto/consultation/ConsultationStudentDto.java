package kr.or.ddit.finalProject.dto.consultation;

import lombok.Data;

/**
 * 상담 작성/학생별 조회용 학생 모달 검색 결과 DTO
 * STUDENT + MEMBER(학생/학부모) + CLASSROOM 조인 결과
 * · 학생 선택 시 학부모명 자동 매칭에도 사용
 */
@Data
public class ConsultationStudentDto {

    private String stdUserId;   // 학생 ID
    private String studentNm;   // 학생명
    private String studentTel;  // 학생 연락처
    private String enrlSchlNm;  // 재학 학교명
    private String className;   // 담당 반/강좌명 (CLASSROOM.CLASS_NM)
    private String parentNm;    // 학부모명 (자동 매칭 대상)
    private String parentTel;   // 학부모 연락처 (STUDENT.PRNT_TELNO)
    private Integer cnslCnt;    // 누적 상담 횟수
}
