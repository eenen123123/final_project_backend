package kr.or.ddit.finalProject.service.instructor;

import java.util.List;

import kr.or.ddit.finalProject.dto.instructor.journal.InstructorJournalDto;

/**
 * 업무 일지 서비스 인터페이스
 *
 * 접근 제어 정책:
 *   - 강사 본인(isViewer=false) : 본인 일지만 조회
 *   - 팀 매니저(isViewer=true, mgrUserId!=null) : 자신의 팀 강사 일지만 조회
 *       EMPLOYEE_INFO.MNT_USER_ID 계층(CONNECT BY)으로 팀원 범위 결정
 *   - 원장 등 최상위 뷰어(isViewer=true, mgrUserId=null) : 전체 일지 조회
 *
 * 소유권 검증(수정·삭제 전 작성자 확인)도 이 레이어에서 수행합니다.
 */
public interface InstructorJournalService {

    int PAGE_SIZE = 15;

    /**
     * 일지 목록 조회 (검색 필터 + 페이지네이션)
     *
     * @param userId          로그인 사용자 ID
     * @param isViewer        true이면 뷰어(팀 매니저/원장), false이면 본인 목록
     * @param mgrUserId       팀 매니저의 USER_ID (null이면 팀 필터 없음 — 원장 등)
     * @param selectedInstrId 뷰어가 선택한 강사 ID (null 또는 빈 문자열이면 전체)
     * @param keyword         제목 키워드 (null 또는 빈 문자열이면 전체)
     * @param fromDt          시작일 yyyy-MM-dd (null이면 제한 없음)
     * @param toDt            종료일 yyyy-MM-dd (null이면 제한 없음)
     * @param page            1-based 페이지 번호
     */
    List<InstructorJournalDto> retrieveJournalList(
            String userId, boolean isViewer, String mgrUserId, String selectedInstrId,
            String keyword, String fromDt, String toDt, int page);

    /**
     * 일지 전체 건수 조회 (필터 조건 동일하게 적용)
     */
    int retrieveJournalCount(
            String userId, boolean isViewer, String mgrUserId, String selectedInstrId,
            String keyword, String fromDt, String toDt);

    /**
     * 일지를 한 건 이상 작성한 강사 목록 (뷰어 필터 드롭다운용)
     *
     * @param mgrUserId 팀 매니저의 USER_ID (null이면 전체 강사 반환)
     */
    List<InstructorJournalDto> retrieveJournalInstructors(String mgrUserId);

    /**
     * 일지 단건 상세 조회
     *
     * @param jrnlSn 조회할 일지 일련번호
     */
    InstructorJournalDto retrieveJournalBySn(Long jrnlSn);

    /**
     * 일지 등록
     *
     * @param dto 등록할 일지 데이터 (instrUserId는 컨트롤러에서 주입)
     * @return 생성된 일지의 JRNL_SN
     */
    Long createJournal(InstructorJournalDto dto);

    /**
     * 일지 수정
     * 작성자(instrUserId)와 로그인 사용자가 다르면 예외를 던집니다.
     *
     * @param dto    수정할 데이터
     * @param userId 로그인 사용자 ID (소유권 검증용)
     */
    void modifyJournal(InstructorJournalDto dto, String userId);

    /**
     * 일지 삭제
     * 작성자(instrUserId)와 로그인 사용자가 다르면 예외를 던집니다.
     *
     * @param jrnlSn 삭제할 일지 일련번호
     * @param userId 로그인 사용자 ID (소유권 검증용)
     */
    void removeJournal(Long jrnlSn, String userId);
}
