package kr.or.ddit.finalProject.service.enrollment;

import java.util.List;

import kr.or.ddit.finalProject.dto.enrollment.CourseEnrollmentDto;

public interface CourseEnrollmentService {

    /**
     * 강좌 수강권한 부여 또는 연장. 결제 확정 시 호출.
     * 기존 권한이 없으면 결제일+1년으로 신규 부여, 있으면 만기를 1년 연장한다.
     */
    void grantOrExtend(String userId, Long courseSn, Long ordSn);

    /**
     * 현재 강좌를 볼 수 있는지 여부 (ACTIVE && 만기 이내).
     */
    boolean hasActiveAccess(String userId, Long courseSn);

    /**
     * 내 수강 중 강좌 목록 조회 (ACTIVE 상태만).
     */
    List<CourseEnrollmentDto> getMyEnrolledCourses(String userId);

    /**
     * 수강 회수: 취소/환불 승인 시 해당 주문으로 부여된 수강권을 REVOKED 처리.
     */
    void revokeByOrdSn(Long ordSn);
}
