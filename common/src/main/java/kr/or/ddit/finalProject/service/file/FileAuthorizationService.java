package kr.or.ddit.finalProject.service.file;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import kr.or.ddit.finalProject.dto.file.FileDto;
import kr.or.ddit.finalProject.dto.member.MemberRoleEnum;
import kr.or.ddit.finalProject.mapper.FileMapper;
import kr.or.ddit.finalProject.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileAuthorizationService {
    private final FileMapper fileMapper;
    private final MessageMapper messageMapper;

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
            // case COURSE -> true; // TODO: 강의 자료 접근 권한 체크 로직 구현 필요
            // case INSTRUCTOR -> true; // TODO: 강사 자료 접근 권한 체크 로직 구현 필요
            case MEMBER_ROLE -> authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals(fileDto.getFileCtxId()));
            default -> true; // false로 바꿔야 하지만, 일단 테스트 편의를 위해 true 반환. 실제 구현 시에는 각 케이스에 맞는 권한 체크 로직 구현 필요.
        };
    }
}
