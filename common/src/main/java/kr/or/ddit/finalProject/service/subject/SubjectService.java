package kr.or.ddit.finalProject.service.subject;

import java.util.List;

import kr.or.ddit.finalProject.dto.subject.SubjectClassificationDto;
import kr.or.ddit.finalProject.dto.subject.SubjectDto;

public interface SubjectService {

    // ── 대분류 ──────────────────────────────────────────────────────

    List<SubjectClassificationDto> retrieveClassificationList();

    void createClassification(SubjectClassificationDto dto, String currentUserId);

    void modifyClassification(SubjectClassificationDto dto, String currentUserId);

    /** 소속 과목이 있으면 IllegalStateException을 던진다. */
    void removeClassification(Long subjClId, String currentUserId);

    // ── 과목 ────────────────────────────────────────────────────────

    List<SubjectDto> retrieveSubjectList(Long subjClId);

    void createSubject(SubjectDto dto, String currentUserId);

    void modifySubject(SubjectDto dto, String currentUserId);

    /** 연결된 강좌가 있으면 IllegalStateException을 던진다. */
    void removeSubject(Long subjId, Long subjClId, String currentUserId);
}
