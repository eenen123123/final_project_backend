package kr.or.ddit.finalProject.mapper.classroom;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.classroom.ClassroomGradeDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomListResponse;
import kr.or.ddit.finalProject.dto.classroom.ClassroomMemberListResponse;

@Mapper
public interface ClassroomMemberMapper {

    /** 클래스에 등록된 수강생 목록 조회 - 페이징 */
    List<ClassroomMemberListResponse> selectMembersByClassSn(
            @Param("classSn") Long classSn,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    /** 클래스 수강생 총 인원 */
    int countMembersByClassSn(@Param("classSn") Long classSn);

    /** 클래스 내 전체 수강생의 개인별 강의 진도율 조회 (탈퇴/취소 수강생 포함) */
    List<ClassroomMemberListResponse> selectProgressRatesByClassSn(@Param("classSn") Long classSn);

    List<ClassroomGradeDto> selectGradeList(
            @Param("classSn") Long classSn,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize);

    int countGradeList(@Param("classSn") Long classSn);

    List<ClassroomListResponse> selectClassroomsByUserId(@Param("userId") String userId);

    // 파일 ID로 강의 조회 -> 강의로 강좌 조회 -> 강좌로 클래스룸 조회 -> 클래스룸에 학생이 포함되어있는지?
    int existsByFileIdAndUserId(@Param("fileId") Long fileId, @Param("userId") String userId);
}
