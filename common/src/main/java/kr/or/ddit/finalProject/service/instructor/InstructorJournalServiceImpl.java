package kr.or.ddit.finalProject.service.instructor;

import java.util.List;

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
            String userId, boolean isViewer, String keyword, String fromDt, String toDt, int page) {
        String instrUserId = isViewer ? null : userId;
        int offset = (page - 1) * PAGE_SIZE;
        return journalMapper.selectJournalList(instrUserId, keyword, fromDt, toDt, offset, PAGE_SIZE);
    }

    @Override
    public int retrieveJournalCount(
            String userId, boolean isViewer, String keyword, String fromDt, String toDt) {
        String instrUserId = isViewer ? null : userId;
        return journalMapper.selectJournalCount(instrUserId, keyword, fromDt, toDt);
    }

    @Override
    public InstructorJournalDto retrieveJournalBySn(Long jrnlSn) {
        return journalMapper.selectJournalBySn(jrnlSn);
    }

    @Override
    @Transactional
    public Long createJournal(InstructorJournalDto dto) {
        journalMapper.insertJournal(dto);
        // useGeneratedKeys=true 설정으로 INSERT 후 dto.jrnlSn 에 PK가 자동 주입됨
        return dto.getJrnlSn();
    }

    @Override
    @Transactional // select + update 두 번의 DB 작업을 하나의 트랜잭션으로 묶음
    public void modifyJournal(InstructorJournalDto dto, String userId) {
        InstructorJournalDto existing = journalMapper.selectJournalBySn(dto.getJrnlSn());
        if (existing == null) {
            throw new FinalProjectException(ErrorCode.JOURNAL_NOT_FOUND);
        }
        if (!existing.getInstrUserId().equals(userId)) {
            throw new FinalProjectException(ErrorCode.JOURNAL_ACCESS_DENIED);
        }
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
        journalMapper.deleteJournal(jrnlSn);
    }
}
