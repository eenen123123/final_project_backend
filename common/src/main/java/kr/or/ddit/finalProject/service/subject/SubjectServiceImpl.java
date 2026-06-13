package kr.or.ddit.finalProject.service.subject;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.subject.SubjectClassificationDto;
import kr.or.ddit.finalProject.dto.subject.SubjectDto;
import kr.or.ddit.finalProject.mapper.subject.SubjectMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectServiceImpl implements SubjectService {

    private final SubjectMapper subjectMapper;

    // ── 대분류 ──────────────────────────────────────────────────────

    @Override
    public List<SubjectClassificationDto> retrieveClassificationList() {
        return subjectMapper.selectClassificationList();
    }

    @Override
    @Transactional
    public void createClassification(SubjectClassificationDto dto, String currentUserId) {
        dto.setRgtrId(currentUserId);
        dto.setLastMdfrId(currentUserId);
        subjectMapper.insertClassification(dto);
    }

    @Override
    @Transactional
    public void modifyClassification(SubjectClassificationDto dto, String currentUserId) {
        verifyClassificationExists(dto.getSubjClId());
        dto.setLastMdfrId(currentUserId);
        subjectMapper.updateClassification(dto);
    }

    @Override
    @Transactional
    public void removeClassification(Long subjClId, String currentUserId) {
        verifyClassificationExists(subjClId);
        if (subjectMapper.countSubjectsByClassification(subjClId) > 0) {
            throw new IllegalStateException("소속 과목이 있는 대분류는 삭제할 수 없습니다.");
        }
        subjectMapper.deleteClassificationLogically(subjClId, currentUserId);
    }

    // ── 과목 ────────────────────────────────────────────────────────

    @Override
    public List<SubjectDto> retrieveSubjectList(Long subjClId) {
        return subjectMapper.selectSubjectList(subjClId);
    }

    @Override
    @Transactional
    public void createSubject(SubjectDto dto, String currentUserId) {
        verifyClassificationExists(dto.getSubjClId());
        dto.setRgtrId(currentUserId);
        dto.setLastMdfrId(currentUserId);
        subjectMapper.insertSubject(dto);
    }

    @Override
    @Transactional
    public void modifySubject(SubjectDto dto, String currentUserId) {
        verifySubjectExists(dto.getSubjId(), dto.getSubjClId());
        dto.setLastMdfrId(currentUserId);
        subjectMapper.updateSubject(dto);
    }

    @Override
    @Transactional
    public void removeSubject(Long subjId, Long subjClId, String currentUserId) {
        verifySubjectExists(subjId, subjClId);
        if (subjectMapper.countCoursesBySubject(subjId) > 0) {
            throw new IllegalStateException("연결된 강좌가 있는 과목은 삭제할 수 없습니다.");
        }
        subjectMapper.deleteSubjectLogically(subjId, subjClId, currentUserId);
    }

    // ── 내부 검증 ────────────────────────────────────────────────────

    private void verifyClassificationExists(Long subjClId) {
        if (subjectMapper.selectClassificationById(subjClId) == null) {
            throw new IllegalArgumentException("존재하지 않는 대분류입니다.");
        }
    }

    private void verifySubjectExists(Long subjId, Long subjClId) {
        if (subjectMapper.selectSubjectById(subjId, subjClId) == null) {
            throw new IllegalArgumentException("존재하지 않는 과목입니다.");
        }
    }
}
