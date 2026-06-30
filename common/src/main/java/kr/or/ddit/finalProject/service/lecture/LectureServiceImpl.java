package kr.or.ddit.finalProject.service.lecture;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.lecture.LectureDto;
import kr.or.ddit.finalProject.dto.lecture.LectureResponseDto;
import kr.or.ddit.finalProject.dto.lecture.LectureProgressDetailResponse;
import kr.or.ddit.finalProject.dto.lecture.StudentLectureProgressResponse;
import kr.or.ddit.finalProject.mapper.lecture.LectureMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LectureServiceImpl implements LectureService {

    private final LectureMapper lectureMapper;

    @Override
    public List<LectureDto> retrieveLectureByCourseSn(Long courseSn) {
        return lectureMapper.selectLectureByCourseSn(courseSn);
    }

    @Override
    public LectureDto retrieveLectureBySn(Long lectureSn) {
        return lectureMapper.selectLectureBySn(lectureSn);
    }

    @Override
    @Transactional
    public boolean createLecture(LectureDto lectureDto) {
        return lectureMapper.insertLecture(lectureDto) > 0;
    }

    @Override
    @Transactional
    public void modifyLecture(LectureDto lectureDto, String currentUserId) {
        LectureDto original = lectureMapper.selectLectureBySn(lectureDto.getLectureSn());
        if (original == null) {
            throw new IllegalArgumentException("존재하지 않는 강의입니다.");
        }
        if (!currentUserId.equals(original.getRgtrId())) {
            throw new SecurityException("본인이 작성한 강의만 수정할 수 있습니다.");
        }
        lectureDto.setLastMdfrId(currentUserId);
        lectureMapper.updateLecture(lectureDto);
    }

    @Override
    @Transactional
    public void updateLectureProgress(Long lectureId, Long courseId, Integer progress, String userId) {
        // TODO: 강의를 수강하는 사용자가 해당 강의에 접근 권한이 있는지 확인하는 로직 추가 필요

        lectureMapper.updateLectureProgress(lectureId, courseId, progress, userId);

    }

    @Override
    @Transactional
    public void removeLecture(Long lectureSn, String currentUserId) {
        LectureDto original = lectureMapper.selectLectureBySn(lectureSn);
        if (original == null) {
            throw new IllegalArgumentException("존재하지 않는 강의입니다.");
        }
        if (!currentUserId.equals(original.getRgtrId())) {
            throw new SecurityException("본인이 작성한 강의만 삭제할 수 있습니다.");
        }
        lectureMapper.deleteLecture(lectureSn);
    }

    @Override
    public List<LectureResponseDto> retrieveLectureListByCourseSn(Long courseSn, String userId) {
        var result = lectureMapper.selectLectureListByCourseSn(courseSn, userId);
        log.info("result : {}", result.size());
        return result;
    }

    @Override
    @Transactional
    public void toggleOpnnYn(Long lectureSn, String userId) {
        LectureDto lecture = lectureMapper.selectLectureBySn(lectureSn);
        if (lecture == null) throw new IllegalArgumentException("존재하지 않는 강의입니다.");
        String toggled = "Y".equals(lecture.getOpnnYn()) ? "N" : "Y";
        lectureMapper.updateOpnnYn(lectureSn, toggled, userId);
    }

    @Override
    @Transactional
    public void toggleLockYn(Long lectureSn, String userId) {
        LectureDto lecture = lectureMapper.selectLectureBySn(lectureSn);
        if (lecture == null) throw new IllegalArgumentException("존재하지 않는 강의입니다.");
        String toggled = "Y".equals(lecture.getLockYn()) ? "N" : "Y";
        lectureMapper.updateLockYn(lectureSn, toggled, userId);
    }

    @Override
    public List<StudentLectureProgressResponse> retrieveStudentProgressByLecture(Long classSn, Long lectureSn) {
        return lectureMapper.selectStudentProgressByLecture(classSn, lectureSn);
    }

    @Override
    public List<LectureProgressDetailResponse> retrieveLectureProgressByStudent(Long classSn, String userId) {
        return lectureMapper.selectLectureProgressByStudent(classSn, userId);
    }

}
