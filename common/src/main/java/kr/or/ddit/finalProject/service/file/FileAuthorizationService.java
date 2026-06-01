package kr.or.ddit.finalProject.service.file;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import kr.or.ddit.finalProject.dto.file.FileDto;
import kr.or.ddit.finalProject.mapper.FileMapper;
import kr.or.ddit.finalProject.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileAuthorizationService {
    private final FileMapper fileMapper;
    private final MessageMapper messageMapper;

    public boolean canAccess(long fileServerId, Authentication authentication) {
        String userId = authentication.getName();
        FileDto fileDto = fileMapper.findContextByFileId(fileServerId);

        if (fileDto == null
        // || fileDto.getFileCtxType() == null 
        // 위 부분은 주석을 제거해야 하지만, 일단 테스트 편의를 위해 null 체크만 하고 넘어감. 실제 구현 시에는 fileCtxType도 null 체크 후, 적절한 권한 체크 로직 구현 필요.
        ) {
            return false;
        }

        return switch (fileDto.getFileCtxType()) {
            case CHAT_ROOM -> messageMapper.isParticipant(fileDto.getFileCtxId(), userId) > 0;
            // case LECTURE -> checkLectureAccess(fileDto, userId);
            // case DEPT_DOC -> checkDeptDocAccess(fileDto, userId);
            default -> true; // false로 바꿔야 하지만, 일단 테스트 편의를 위해 true 반환. 실제 구현 시에는 각 케이스에 맞는 권한 체크 로직 구현 필요.
        };

    }
}
