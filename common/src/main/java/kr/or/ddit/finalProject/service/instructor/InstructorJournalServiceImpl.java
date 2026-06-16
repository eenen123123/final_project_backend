package kr.or.ddit.finalProject.service.instructor;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.instructor.journal.InstructorJournalDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.instructor.InstructorJournalMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 조회 메서드는 읽기 전용 트랜잭션 (다른 ServiceImpl과 동일한 패턴)
public class InstructorJournalServiceImpl implements InstructorJournalService {

    private final InstructorJournalMapper journalMapper;

    @Override
    public List<InstructorJournalDto> retrieveJournalList(
            String userId, boolean isViewer, String selectedInstrId,
            String keyword, String fromDt, String toDt, int page) {
        String instrUserId = resolveInstrUserId(userId, isViewer, selectedInstrId);
        int offset = (page - 1) * PAGE_SIZE;
        return journalMapper.selectJournalList(instrUserId, keyword, fromDt, toDt, offset, PAGE_SIZE);
    }

    @Override
    public int retrieveJournalCount(
            String userId, boolean isViewer, String selectedInstrId,
            String keyword, String fromDt, String toDt) {
        String instrUserId = resolveInstrUserId(userId, isViewer, selectedInstrId);
        return journalMapper.selectJournalCount(instrUserId, keyword, fromDt, toDt);
    }

    @Override
    public List<InstructorJournalDto> retrieveJournalInstructors() {
        return journalMapper.selectJournalInstructors();
    }

    private static final Safelist JOURNAL_SAFELIST = Safelist.relaxed().addTags("s", "u");

    /** TipTap 에디터 출력 HTML을 허용 태그 목록 기준으로 새니타이징 */
    private String sanitizeHtml(String html) {
        if (html == null) return null;
        return Jsoup.clean(html, JOURNAL_SAFELIST);
    }

    /**
     * 역할과 뷰어 선택에 따라 mapper에 전달할 instrUserId 결정
     *   - 일반 강사: 항상 본인 ID
     *   - 뷰어 + 강사 선택: 선택된 강사 ID
     *   - 뷰어 + 전체: null (필터 없음)
     */
    private String resolveInstrUserId(String userId, boolean isViewer, String selectedInstrId) {
        if (!isViewer) return userId;
        return (selectedInstrId != null && !selectedInstrId.isBlank()) ? selectedInstrId : null;
    }

    @Override
    public InstructorJournalDto retrieveJournalBySn(Long jrnlSn) {
        return journalMapper.selectJournalBySn(jrnlSn);
    }

    @Override
    @Transactional
    public Long createJournal(InstructorJournalDto dto) {
        dto.setJrnlCont(sanitizeHtml(dto.getJrnlCont()));
        journalMapper.insertJournal(dto);
        return dto.getJrnlSn();
    }

    @Override
    @Transactional
    public void modifyJournal(InstructorJournalDto dto, String userId) {
        InstructorJournalDto existing = journalMapper.selectJournalBySn(dto.getJrnlSn());
        if (existing == null) {
            throw new FinalProjectException(ErrorCode.JOURNAL_NOT_FOUND);
        }
        if (!existing.getInstrUserId().equals(userId)) {
            throw new FinalProjectException(ErrorCode.JOURNAL_ACCESS_DENIED);
        }
        dto.setJrnlCont(sanitizeHtml(dto.getJrnlCont()));
        journalMapper.updateJournal(dto);
    }

    @Override
    @Transactional // select + delete 두 번의 DB 작업을 하나의 트랜잭션으로 묶음
    public void removeJournal(Long jrnlSn, String userId) {
        InstructorJournalDto existing = journalMapper.selectJournalBySn(jrnlSn);
        if (existing == null) {
            throw new FinalProjectException(ErrorCode.JOURNAL_NOT_FOUND);
        }
        if (!existing.getInstrUserId().equals(userId)) {
            throw new FinalProjectException(ErrorCode.JOURNAL_ACCESS_DENIED);
        }
        journalMapper.deleteJournal(jrnlSn, userId);
    }
}
