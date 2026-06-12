package kr.or.ddit.finalProject.mapper.enrollment;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.enrollment.CourseEnrollmentDto;

@Mapper
public interface CourseEnrollmentMapper {

    // 재구매 시 insert/연장 판단용 (회원·강좌당 1행)
    CourseEnrollmentDto selectByUserAndCourse(@Param("userId") String userId,
            @Param("courseSn") Long courseSn);

    // 최초 부여: ACCS_END_DT = 결제일 + 1년
    int insertEnrollment(@Param("userId") String userId, @Param("courseSn") Long courseSn,
            @Param("ordSn") Long ordSn);

    // 재구매 연장: 남은 기간을 보존하며 1년 추가, 상태 ACTIVE로 복구
    int extendEnrollment(@Param("userId") String userId, @Param("courseSn") Long courseSn,
            @Param("ordSn") Long ordSn);

    // 권한 확인: ACTIVE && 만기 이내면 1 이상
    int countActiveAccess(@Param("userId") String userId, @Param("courseSn") Long courseSn);
}
