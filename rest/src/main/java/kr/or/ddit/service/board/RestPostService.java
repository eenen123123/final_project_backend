package kr.or.ddit.service.board;

import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.or.ddit.finalProject.dto.board.BoardDto;
import kr.or.ddit.finalProject.dto.board.EditorPostRequestDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.FileMapper;
import kr.or.ddit.finalProject.mapper.board.BoardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestPostService {

    private final FileMapper fileMapper;
    private final BoardMapper boardMapper;
    private final ObjectMapper objectMapper;

    public Map<String, Object> getPost(long postSn) {
        BoardDto board = boardMapper.selectBoardByPostSn(postSn);
        if (board == null) {
            throw new FinalProjectException(ErrorCode.POST_NOT_FOUND);
        }
        try {
            // postCn은 TipTap JSON 문자열 → JsonNode로 파싱해서 반환 (프론트에서 바로 initialContent로 사용)
            return Map.of(
                "postSn", board.getPostSn(),
                "postSj", board.getPostSj(),
                "postCn", objectMapper.readTree(board.getPostCn())
            );
        } catch (JsonProcessingException e) {
            throw new FinalProjectException(ErrorCode.JSON_PROCESSING_FAILED, e);
        }
    }

    /**
     * @param boardTypeCd BOARD_TYPE_CD 컬럼 값 (호출하는 컨트롤러에서 맥락에 맞게 전달)
     */
    public long createPost(EditorPostRequestDto req, String boardTypeCd,
            Authentication authentication) {
        String userId = authentication.getName();
        List<Long> fileIds = req.getFileIds();

        // 1. fileIds 소유자 배치 검증 — 하나라도 불일치하면 403
        if (fileIds != null && !fileIds.isEmpty()) {
            int ownedCount = fileMapper.countOwnedFiles(fileIds, userId);
            if (ownedCount != fileIds.size()) {
                throw new FinalProjectException(ErrorCode.FILE_ACCESS_DENIED);
            }
        }

        // 2. TipTap JsonNode → String 직렬화
        String contentJson;
        try {
            contentJson = objectMapper.writeValueAsString(req.getPostCn());
            log.info("Serialized post content JSON: {}", contentJson);
        } catch (JsonProcessingException e) {
            throw new FinalProjectException(ErrorCode.JSON_PROCESSING_FAILED, e);
        }

        // 3. BOARD INSERT — selectKey가 INSERT 전에 postSn을 채번해 boardDto에 세팅함
        BoardDto boardDto = BoardDto.builder().wrtrUserId(userId).boardTypeCd(boardTypeCd)
                .postSj(req.getPostSj()).postCn(contentJson).build();
        boardMapper.insertBoard(boardDto);
        long postSn = boardDto.getPostSn();

        // 4. 이미지 파일 CTX 업데이트
        if (fileIds != null && !fileIds.isEmpty()) {
            fileMapper.updateFileContext(fileIds, "POST", postSn);
        }

        return postSn;
    }
}
