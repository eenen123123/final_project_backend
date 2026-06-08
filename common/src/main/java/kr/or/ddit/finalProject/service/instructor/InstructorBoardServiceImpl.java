package kr.or.ddit.finalProject.service.instructor;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import kr.or.ddit.finalProject.dto.instructor.InstructorBoardDto;
import kr.or.ddit.finalProject.dto.instructor.InstructorBoardResponse;
import kr.or.ddit.finalProject.mapper.instructor.InstructorBoardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstructorBoardServiceImpl implements InstructorBoardService {

    private final InstructorBoardMapper instructorBoardMapper;

    @Override
    public List<InstructorBoardResponse> getInstructorBoardList(String instrUserId) {
        List<InstructorBoardDto> original = instructorBoardMapper.selectInstructorBoardList(instrUserId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<InstructorBoardResponse> response
                = original.stream()
                        .map(dto -> {
                            String userName = dto.getMemberDto() != null ? dto.getMemberDto().getUserName() : "";
                            InstructorBoardResponse responseDto = new InstructorBoardResponse();
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
    public InstructorBoardResponse getInstructorBoardDetail(Long postSn, String instrUserId) {
        InstructorBoardDto original = instructorBoardMapper.selectInstructorBoardDetail(postSn, instrUserId);
        if (original == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String userName = original.getMemberDto() != null ? original.getMemberDto().getUserName() : "";
        return InstructorBoardResponse.builder()
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

    // ── 클래스룸 공지사항 ──────────────────────────────────────────

    @Override
    public List<InstructorBoardDto> getClassroomNoticeList(Long classSn) {
        return instructorBoardMapper.selectClassroomNoticeList(classSn);
    }

    @Override
    public InstructorBoardDto getClassroomNoticeDetail(Long postSn, Long classSn) {
        return instructorBoardMapper.selectClassroomNoticeDetail(postSn, classSn);
    }

    @Override
    public int insertClassroomNotice(InstructorBoardDto dto) {
        dto.setBoardTypeCd("02");
        return instructorBoardMapper.insertClassroomNotice(dto);
    }

    @Override
    public int deleteClassroomNotice(Long postSn, Long classSn) {
        return instructorBoardMapper.deleteClassroomNotice(postSn, classSn);
    }

    // ── 클래스룸 Q&A ──────────────────────────────────────────────

    @Override
    public List<kr.or.ddit.finalProject.dto.classroom.ClassroomQnaDto> getClassroomQnaList(Long classSn) {
        return instructorBoardMapper.selectClassroomQnaList(classSn);
    }

    @Override
    public kr.or.ddit.finalProject.dto.classroom.ClassroomQnaDto getClassroomQnaDetail(Long postSn, Long classSn) {
        return instructorBoardMapper.selectClassroomQnaDetail(postSn, classSn);
    }

    @Override
    public void insertClassroomQna(InstructorBoardDto dto) {
        dto.setBoardTypeCd("03");
        instructorBoardMapper.insertClassroomQnaBoard(dto);
        instructorBoardMapper.insertClassroomQnaChild(dto.getPostSn());
    }

    @Override
    public void answerClassroomQna(Long postSn, String answrUserId, String answCn) {
        instructorBoardMapper.updateClassroomQnaAnswer(postSn, answrUserId, answCn);
    }

    @Override
    public int getUnansweredQnaCount(Long classSn) {
        return instructorBoardMapper.selectUnansweredQnaCount(classSn);
    }

}
