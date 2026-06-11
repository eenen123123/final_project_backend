package kr.or.ddit.finalProject.service.file;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import kr.or.ddit.finalProject.dto.file.FileDto;
import kr.or.ddit.finalProject.dto.member.MemberRoleEnum;
import kr.or.ddit.finalProject.mapper.FileMapper;
import kr.or.ddit.finalProject.mapper.MessageMapper;
import kr.or.ddit.finalProject.mapper.classroom.ClassroomMemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileAuthorizationService {
    private final FileMapper fileMapper;
    private final MessageMapper messageMapper;
    private final ClassroomMemberMapper classroomMemberMapper;

    public boolean canAccess(long fileServerId, Authentication authentication) {

        String userId = authentication.getName();
        FileDto fileDto = fileMapper.findContextByFileServerId(fileServerId);

        if (fileDto == null || fileDto.getFileCtxType() == null || fileDto.getFileCtxId() == null) {
            return false;
        }

        return switch (fileDto.getFileCtxType()) {
            case CHAT_ROOM -> messageMapper.isParticipant(Long.parseLong(fileDto.getFileCtxId()),
                    userId) > 0;
            // case DEPT_DOC -> checkDeptDocAccess(fileDto, userId);
            case COURSE -> checkCourseAccess(authentication, fileServerId);
            // case INSTRUCTOR -> true; // TODO: 강사 자료 접근 권한 체크 로직 구현 필요
            case MEMBER_ROLE -> checkRole(authentication, fileDto.getFileCtxId());
            default -> true; // false로 바꿔야 하지만, 일단 테스트 편의를 위해 true 반환. 실제 구현 시에는 각 케이스에 맞는 권한 체크 로직 구현 필요.
        };
    }

    public boolean canAccess(long[] fileServerIds, Authentication authentication) {
        for (long fileServerId : fileServerIds) {
            if (!canAccess(fileServerId, authentication)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkRole(Authentication authentication, String requiredRole) {
        if (authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(MemberRoleEnum.ROLE_ADMIN.name()))) {
            return true; // 관리자 권한이 있으면 모든 파일 접근 허용
        }

        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(requiredRole));
    }

    private boolean checkCourseAccess(Authentication authentication, long fileServerId) {
        List<MemberRoleEnum> userRoles = authentication.getAuthorities().stream()
                .map(auth -> MemberRoleEnum.valueOf(auth.getAuthority()))
                .toList();
        switch (userRoles) {
            case List<MemberRoleEnum> r when r.contains(MemberRoleEnum.ROLE_ADMIN):
                return true; // 관리자 권한이 있으면 모든 강의 자료 접근 허용
            case List<MemberRoleEnum> r when r.contains(MemberRoleEnum.ROLE_STUDENT): {
                // 파일 ID로 강의 조회 -> 강의로 강좌 조회 -> 강좌로 클래스룸 조회 -> 클래스룸에 학생이 포함되어있는지?
                return classroomMemberMapper.existsByFileIdAndUserId(fileServerId,
                        authentication.getName()) > 0;
            }
            default:
                break;
        }

        return false; // TODO: 실제 강의 자료 접근 권한 체크 로직 구현 필요
    }
}
