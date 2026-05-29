package kr.or.ddit.finalProject.service.classroom;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.classroom.ClassroomListResponse;
import kr.or.ddit.finalProject.mapper.classroom.ClassroomMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomMapper classroomMapper;

    @Override
    public List<ClassroomListResponse> retrieveClassroomList(String instrUserId) {
        return classroomMapper.selectClassroomListByInstructor(instrUserId);
    }

}
