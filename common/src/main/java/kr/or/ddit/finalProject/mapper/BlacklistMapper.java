package kr.or.ddit.finalProject.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.student.StudentBlackListDto;
import kr.or.ddit.finalProject.dto.student.StudentBlackListHistoryDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

/**
 * 주의 학생(블랙리스트) 매퍼.
 *
 * - 현재 상태   : STUDENT_BLACK_LIST
 * - 변경 이력   : STUDENT_BLACK_LIST_HISTORY
 * - 표시 조인   : STUDENT / MEMBER / CLASSROOM(_MEMBER) / COM_CD
 */
@Mapper
public interface BlacklistMapper {

    /** 목록 동적 검색 + 서버 페이징 */
    List<StudentBlackListDto> searchBlacklist(PaginationInfo<Map<String, Object>> paging);

    /** 목록 전체 건수 (페이징용) */
    int countBlacklist(PaginationInfo<Map<String, Object>> paging);

    /** 상단 요약 카드 (전체/고위험/관찰/해제) */
    Map<String, Object> selectBlacklistSummary();

    /** 단일 상세 (현재 상태 + 조인) */
    StudentBlackListDto selectBlacklistDetail(@Param("stdUserId") String stdUserId);

    /** 변경 이력 목록 (최신순) */
    List<StudentBlackListHistoryDto> selectBlacklistHistory(@Param("stdUserId") String stdUserId);

    /** 현재 등록 여부 (등록/수정 분기용) */
    int existsBlacklist(@Param("stdUserId") String stdUserId);

    /** 현재 상태 신규 등록 (시작일 SYSTIMESTAMP, 종료일 = 정지일수 기준 계산) */
    int insertBlacklist(StudentBlackListDto dto);

    /** 재등록 시 현재 상태 재활성 (시작/종료일 갱신 + 속성 갱신) */
    int reactivateBlacklist(StudentBlackListDto dto);

    /** 속성 수정 (날짜 불변) */
    int updateBlacklist(StudentBlackListDto dto);

    /** 해제 처리 (BLKLST_END_DT 설정) */
    int resolveBlacklist(@Param("stdUserId") String stdUserId);

    /** 이력 적재 */
    int insertBlacklistHistory(StudentBlackListHistoryDto dto);

    /** 로그인 차단 판정: 현재 적용 중(영구 또는 미만료) 정지 건수 */
    int countActiveBlock(@Param("userId") String userId);

    /** 누적 위반 횟수 (이력의 REG 이벤트 수) — 자동 에스컬레이션 기준 */
    int countOffenses(@Param("userId") String userId);
}
