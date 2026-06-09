package kr.or.ddit.finalProject.service.instructor;

import java.util.List;

import kr.or.ddit.finalProject.dto.instructor.InstructorJournalDto;

/**
 * 업무 일지 서비스 인터페이스
 *
 * 접근 제어 정책:
 *   - 강사 본인(isViewer=false) : 본인 일지만 조회
 *   - 수석 강사(T001) / 원장(Z001) : 전체 일지 조회 (isViewer=true)
 *
 * 소유권 검증(수정·삭제 전 작성자 확인)도 이 레이어에서 수행합니다.
 */
public interface InstructorJournalService {

    /**
     * 일지 목록 조회
     *
     * @param userId   로그인 사용자 ID
     * @param isViewer true이면 수석 강사/원장 → 전체 목록 반환
     *                 false이면 일반 강사     → 본인 목록 반환
     */
    List<InstructorJournalDto> retrieveJournalList(String userId, boolean isViewer);

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
