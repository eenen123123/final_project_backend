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
                            InstructorBoardResponseDto responseDto = new InstructorBoardResponseDto();
                            responseDto.setPostSn(dto.getPostSn().intValue());
                            responseDto.setUserName(dto.getMemberDto().getUserName()); // 작성자 이름 설정
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
    public InstructorBoardDto getInstructorBoardDetail(int postSn) {
        return instructorBoardMapper.selectInstructorBoardDetail(postSn);
    }

    @Override
    public int insertInstructorBoard(InstructorBoardDto instructorBoardDto) {
        return instructorBoardMapper.insertInstructorBoard(instructorBoardDto);
    }

    @Override
    public int updateInstructorBoard(InstructorBoardDto instructorBoardDto) {
        return 0;
    }

    @Override
    public int deleteInstructorBoard(int postSn) {
        return 0;
    }

}
