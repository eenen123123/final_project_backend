package kr.or.ddit.finalProject.service.classroom;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.classroom.ClassroomDetailResponse;
import kr.or.ddit.finalProject.dto.classroom.ClassroomGradeDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomListResponse;
import kr.or.ddit.finalProject.dto.classroom.ClassroomMemberListResponse;
import kr.or.ddit.finalProject.dto.lecture.LectureProgressDto;
import kr.or.ddit.finalProject.mapper.lecture.LectureMapper;
import kr.or.ddit.finalProject.dto.coursecohort.CourseCohortListResponse;
import kr.or.ddit.finalProject.mapper.classroom.ClassroomMapper;
import kr.or.ddit.finalProject.mapper.classroom.ClassroomMemberMapper;
import kr.or.ddit.finalProject.mapper.coursecohort.CourseCohortMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassroomServiceImpl implements ClassroomService {

    private static final DateTimeFormatter REG_DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ClassroomMapper classroomMapper;
    private final ClassroomMemberMapper classroomMemberMapper;
    private final CourseCohortMapper courseCohortMapper;
    private final LectureMapper lectureMapper;

    @Override
    public List<ClassroomListResponse> retrieveClassroomList(String instrUserId) {
        List<ClassroomListResponse> list = classroomMapper.selectClassroomListByInstructor(instrUserId);
        list.forEach(item -> item.setFormattedRegDt(item.getRegDt().format(REG_DT_FORMAT)));
        return list;
    }

    @Override
    public ClassroomDetailResponse retrieveClassroomDetail(Long classSn) {
        ClassroomDetailResponse detail = classroomMapper.selectClassroomBySn(classSn);
        if (detail == null) {
            throw new IllegalArgumentException("존재하지 않는 클래스룸입니다: " + classSn);
        }

        detail.setEnrlStrtYmd(formatYmd(detail.getEnrlStrtYmd()));
        if (detail.getEnrlEndYmd() != null) {
            detail.setEnrlEndYmd(formatYmd(detail.getEnrlEndYmd()));
        }

        List<ClassroomMemberListResponse> members = classroomMemberMapper.selectMembersByClassSn(classSn);
        members.forEach(m -> m.setFormattedRegDt(m.getRegDt().format(REG_DT_FORMAT)));
        detail.setMembers(members);

        List<CourseCohortListResponse> cohorts = courseCohortMapper.selectCohortsByClassSn(classSn);
        cohorts.forEach(c -> {
            c.setCohortStrtYmd(formatYmd(c.getCohortStrtYmd()));
            if (c.getCohortEndYmd() != null) c.setCohortEndYmd(formatYmd(c.getCohortEndYmd()));
        });
        detail.setCohorts(cohorts);

        return detail;
    }

    @Override
    public List<ClassroomGradeDto> retrieveGradeList(Long classSn) {
        return classroomMemberMapper.selectGradeList(classSn);
    }

    @Override
    public List<LectureProgressDto> retrieveLecturesWithProgress(Long classSn) {
        return lectureMapper.selectLecturesWithProgress(classSn);
    }

    // YYYYMMDD → YYYY.MM.DD
    private String formatYmd(String ymd) {
        return ymd.substring(0, 4) + "." + ymd.substring(4, 6) + "." + ymd.substring(6, 8);
    }

}
