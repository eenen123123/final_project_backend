package kr.or.ddit.finalProject.service.enrollment;

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
}
