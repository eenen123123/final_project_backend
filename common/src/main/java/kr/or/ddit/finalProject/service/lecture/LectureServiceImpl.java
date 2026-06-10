package kr.or.ddit.finalProject.service.lecture;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.lecture.LectureDto;
import kr.or.ddit.finalProject.mapper.lecture.LectureMapper;
import lombok.RequiredArgsConstructor;

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

}
