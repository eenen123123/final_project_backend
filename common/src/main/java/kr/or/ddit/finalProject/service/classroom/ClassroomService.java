package kr.or.ddit.finalProject.service.classroom;

import java.util.List;

import kr.or.ddit.finalProject.dto.classroom.ClassroomDto;
import kr.or.ddit.finalProject.dto.classroom.ClassroomMemberDto;

public interface ClassroomService {

    ClassroomDto retrieveClassroom(Long classId);

    List<ClassroomMemberDto> retrieveMembers(Long classId, boolean includeAll, String requesterId);
}
