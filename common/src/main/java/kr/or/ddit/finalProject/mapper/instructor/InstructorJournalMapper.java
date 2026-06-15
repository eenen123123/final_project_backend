package kr.or.ddit.finalProject.mapper.instructor;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.instructor.journal.InstructorJournalDto;

/**
 * 업무 일지 Mapper
 *
 * 접근 제어는 서비스/컨트롤러 레이어에서 처리하므로
 * 이 Mapper는 역할과 무관하게 순수 DB 조작만 담당합니다.
 */
@Mapper
public interface InstructorJournalMapper {

    /**
     * 일지 목록 조회 (검색 필터 + 페이지네이션)
     *
     * @param instrUserId null이면 전체 강사 조회 (뷰어용), 값이 있으면 해당 강사만 조회
     * @param keyword     제목 키워드 (null 또는 빈 문자열이면 전체)
     * @param fromDt      시작일 yyyy-MM-dd (null이면 제한 없음)
     * @param toDt        종료일 yyyy-MM-dd (null이면 제한 없음)
     * @param offset      건너뛸 행 수 (0-based)
     * @param pageSize    페이지당 조회 건수
     */
    List<InstructorJournalDto> selectJournalList(
            @Param("instrUserId") String instrUserId,
            @Param("keyword")     String keyword,
            @Param("fromDt")      String fromDt,
            @Param("toDt")        String toDt,
            @Param("offset")      int offset,
            @Param("pageSize")    int pageSize);

    /**
     * 일지 전체 건수 조회 (페이지네이션 총 페이지 계산용)
     */
    int selectJournalCount(
            @Param("instrUserId") String instrUserId,
            @Param("keyword")     String keyword,
            @Param("fromDt")      String fromDt,
            @Param("toDt")        String toDt);

    /**
     * 일지를 한 건 이상 작성한 강사 목록 조회 (뷰어 강사 필터 드롭다운용)
     * instrUserId / instrUserNm 필드만 채워서 반환합니다.
     */
    List<InstructorJournalDto> selectJournalInstructors();

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
