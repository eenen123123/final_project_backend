package kr.or.ddit.finalProject.service.staff;

import java.util.List;
import java.util.Map;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.student.StudentBlackListDto;
import kr.or.ddit.finalProject.dto.student.StudentBlackListHistoryDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

/**
 * 주의 학생(블랙리스트) 서비스.
 *
 * - 조회 : 동적 검색 + 서버 페이징, 요약 카드, 상세(현재 상태 + 변경 이력)
 * - 변경 : 등록 / 수정 / 해제 → 현재 상태(STUDENT_BLACK_LIST) 와
 *          이력(STUDENT_BLACK_LIST_HISTORY) 을 한 트랜잭션으로 처리
 */
public interface BlacklistService {

    /** 목록 동적 검색 + 서버 페이징 */
    PageResponse<StudentBlackListDto> searchBlacklist(PaginationInfo<Map<String, Object>> paging);

    /** 상단 요약 카드 (전체/고위험/관찰/해제) */
    Map<String, Object> getSummary();

    /** 단일 상세 (현재 상태) */
    StudentBlackListDto getDetail(String stdUserId);

    /** 변경 이력 목록 */
    List<StudentBlackListHistoryDto> getHistory(String stdUserId);

    /**
     * 위반 등록 (누적 횟수 기반 자동 에스컬레이션).
     * 1회=경고(차단없음), 2회=기간정지, 3회+=영구정지.
     * @return 적용된 페널티 정보 (offenseCount, levelCd, impsDays, blocked, permanent)
     */
    Map<String, Object> registerBlacklist(StudentBlackListDto dto, String loginUserId);

    /** 수정 (현재 상태 + 이력 MOD) */
    void updateBlacklist(StudentBlackListDto dto, String loginUserId);

    /** 해제 (BLKLST_END_DT 설정 + 이력 REL) */
    void resolveBlacklist(String stdUserId, String loginUserId);

    /** 로그인 차단 여부: 현재 적용 중인 정지가 있으면 true */
    boolean isLoginBlocked(String userId);
}
