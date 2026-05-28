package kr.or.ddit.finalProject.service.instructor;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import kr.or.ddit.finalProject.dto.instructor.InstructorBoardDto;
import kr.or.ddit.finalProject.mapper.instructor.InstructorBoardMapper;
import kr.or.ddit.finalProject.responseDto.instructor.InstructorBoardResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstructorBoardServiceImpl implements InstructorBoardService {

    private final InstructorBoardMapper instructorBoardMapper;

    @Override
    public List<InstructorBoardResponseDto> getInstructorBoardList(String instrUserId) {
        List<InstructorBoardDto> original = instructorBoardMapper.selectInstructorBoardList(instrUserId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<InstructorBoardResponseDto> response
                = original.stream()
                        .map(dto -> {
                            String userName = dto.getMemberDto() != null ? dto.getMemberDto().getUserName() : "";
                            InstructorBoardResponseDto responseDto = new InstructorBoardResponseDto();
                            responseDto.setPostSn(dto.getPostSn());
                            responseDto.setUseYn(dto.getUseYn());
                            responseDto.setBoardTypeCd(dto.getBoardTypeCd());
                            responseDto.setBoardTypeNm(dto.getBoardTypeNm());
                            responseDto.setUserName(userName);
                            responseDto.setTitle(dto.getPostSj());
                            responseDto.setContent(dto.getPostCn());
                            responseDto.setRegDt(dto.getRegDt() != null ? dto.getRegDt().format(formatter) : null);
                            responseDto.setMdfcnDt(dto.getMdfcnDt() != null ? dto.getMdfcnDt().format(formatter) : null);
                            responseDto.setAtchFileId(dto.getAtchFileId() != null ? dto.getAtchFileId().toString() : null);
                            return responseDto;
                        })
                        .toList();
        log.info("게시글 목록 조회 : {}", response);
        return response;
    }

    @Override
    public InstructorBoardResponseDto getInstructorBoardDetail(Long postSn, String instrUserId) {
        InstructorBoardDto original = instructorBoardMapper.selectInstructorBoardDetail(postSn, instrUserId);
        if (original == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String userName = original.getMemberDto() != null ? original.getMemberDto().getUserName() : "";
        return InstructorBoardResponseDto.builder()
                .postSn(original.getPostSn())
                .useYn(original.getUseYn())
                .boardTypeCd(original.getBoardTypeCd())
                .boardTypeNm(original.getBoardTypeNm())
                .userName(userName)
                .title(original.getPostSj())
                .content(original.getPostCn())
                .regDt(original.getRegDt() != null ? original.getRegDt().format(formatter) : null)
                .mdfcnDt(original.getMdfcnDt() != null ? original.getMdfcnDt().format(formatter) : null)
                .atchFileId(original.getAtchFileId() != null ? original.getAtchFileId().toString() : null)
                .build();
    }

    @Override
    public int insertInstructorBoard(InstructorBoardDto instructorBoardDto) {
        int rowcnt = instructorBoardMapper.insertInstructorBoard(instructorBoardDto);
        if (rowcnt > 0) {
            log.info("게시글 등록 성공 : {}", instructorBoardDto);
        } else {
            log.warn("게시글 등록 실패 : {}", instructorBoardDto);
        }
        return rowcnt;
    }

    @Override
    public int updateInstructorBoard(InstructorBoardDto instructorBoardDto) {
        int rowcnt = instructorBoardMapper.updateInstructorBoard(instructorBoardDto);
        if (rowcnt > 0) {
            log.info("게시글 수정 성공 : {}", instructorBoardDto);
        } else {
            log.warn("게시글 수정 실패 : {}", instructorBoardDto);
        }
        return rowcnt;
    }

    @Override
    public int deleteInstructorBoard(Long postSn, String instrUserId) {
        return instructorBoardMapper.deleteInstructorBoard(postSn, instrUserId);
    }

    @Override
    public int restoreInstructorBoard(Long postSn, String instrUserId) {
        return instructorBoardMapper.restoreInstructorBoard(postSn, instrUserId);
    }

}
