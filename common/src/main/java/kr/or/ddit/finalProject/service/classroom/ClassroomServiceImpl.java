package kr.or.ddit.finalProject.service.classroom;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.classroom.ClassroomDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomMemberDto;
import kr.or.ddit.finalProject.mapper.classroom.ClassroomMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomMapper classroomMapper;

    @Override
    public ClassroomDto retrieveClassroom(Long classId) {
        ClassroomDto classroom = classroomMapper.selectClassroomById(classId);
        if (classroom == null) {
            throw new IllegalArgumentException("존재하지 않는 클래스룸입니다: " + classId);
        }
        return classroom;
    }

    @Override
    public List<ClassroomMemberDto> retrieveMembers(Long classId, boolean includeAll, String requesterId) {
        if (includeAll) {
            ClassroomDto classroom = classroomMapper.selectClassroomById(classId);
            if (classroom == null) {
                throw new IllegalArgumentException("존재하지 않는 클래스룸입니다: " + classId);
            }
            if (!classroom.getOpnrUserId().equals(requesterId)) {
                throw new SecurityException("강사만 전체 수강 이력을 조회할 수 있습니다.");
            }
            return classroomMapper.selectMembersByClassId(classId, null);
        }
        return classroomMapper.selectMembersByClassId(classId, "01");
    }
}
