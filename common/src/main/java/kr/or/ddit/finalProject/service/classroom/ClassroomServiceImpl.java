package kr.or.ddit.finalProject.service.classroom;

import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter REG_DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ClassroomMapper classroomMapper;

    @Override
    public List<ClassroomListResponse> retrieveClassroomList(String instrUserId) {
        List<ClassroomListResponse> list = classroomMapper.selectClassroomListByInstructor(instrUserId);
        list.forEach(item -> item.setFormattedRegDt(item.getRegDt().format(REG_DT_FORMAT)));
        return list;
    }

}
