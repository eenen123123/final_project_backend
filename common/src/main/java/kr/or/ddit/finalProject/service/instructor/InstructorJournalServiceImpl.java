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
    public List<InstructorJournalDto> retrieveJournalList(String userId, boolean isViewer) {
        // isViewer=true → 수석 강사(T001) 또는 원장(Z001): 전체 일지
        // isViewer=false → 일반 강사: 본인 일지만
        if (isViewer) {
            return journalMapper.selectAllJournalList();
        }
        return journalMapper.selectJournalListByInstructor(userId);
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
