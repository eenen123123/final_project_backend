package kr.or.ddit.finalProject.mapper.instructor;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.instructor.InstructorJournalDto;

/**
 * 업무 일지 Mapper
 *
 * 접근 제어는 서비스/컨트롤러 레이어에서 처리하므로
 * 이 Mapper는 역할과 무관하게 순수 DB 조작만 담당합니다.
 */
@Mapper
public interface InstructorJournalMapper {

    /**
     * 특정 강사의 일지 목록 조회 (날짜 내림차순)
     * 강사 본인 조회에 사용합니다.
     */
    List<InstructorJournalDto> selectJournalListByInstructor(@Param("instrUserId") String instrUserId);

    /**
     * 전체 강사의 일지 목록 조회 (날짜 내림차순)
     * 수석 강사(T001) / 원장(Z001) 조회에 사용합니다.
     */
    List<InstructorJournalDto> selectAllJournalList();

    /**
     * 일지 단건 상세 조회
     *
     * @param jrnlSn 조회할 일지 일련번호
     */
    InstructorJournalDto selectJournalBySn(@Param("jrnlSn") Long jrnlSn);

    /**
     * 업무 일지 등록
     * INSERT 후 생성된 JRNL_SN 이 dto.jrnlSn 에 자동으로 채워집니다 (useGeneratedKeys).
     */
    void insertJournal(InstructorJournalDto dto);

    /**
     * 업무 일지 수정 (제목·본문·날짜 변경 가능)
     * 소유권 확인(instrUserId 일치 여부)은 서비스에서 합니다.
     */
    void updateJournal(InstructorJournalDto dto);

    /**
     * 업무 일지 삭제
     * 소유권 확인은 서비스에서 합니다.
     */
    void deleteJournal(@Param("jrnlSn") Long jrnlSn);
}
