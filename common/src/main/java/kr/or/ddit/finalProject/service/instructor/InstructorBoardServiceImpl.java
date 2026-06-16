package kr.or.ddit.finalProject.service.instructor;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.instructor.board.BoardType;
import kr.or.ddit.finalProject.service.file.FileUploadService;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardDto;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardResponse;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorPublicBoardDetail;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorPublicBoardItem;
import kr.or.ddit.finalProject.mapper.instructor.InstructorBoardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstructorBoardServiceImpl implements InstructorBoardService {

    private final InstructorBoardMapper instructorBoardMapper;
    private final FileUploadService fileUploadService;

    @Override
    public PageResponse<InstructorBoardResponse> getInstructorBoardList(
            String instrUserId, String keyword, String boardTypeCd, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        int totalCount = instructorBoardMapper.selectInstructorBoardCount(instrUserId, keyword, boardTypeCd);
        List<InstructorBoardDto> original = instructorBoardMapper.selectInstructorBoardList(
                instrUserId, keyword, boardTypeCd, offset, pageSize);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<InstructorBoardResponse> items = original.stream()
                .map(dto -> {
                    String userName = dto.getMemberDto() != null ? dto.getMemberDto().getUserName() : "";
                    InstructorBoardResponse responseDto = new InstructorBoardResponse();
                    responseDto.setPostSn(dto.getPostSn());
                    responseDto.setUseYn(dto.getUseYn());
                    responseDto.setBoardTypeCd(dto.getBoardTypeCd());
                    responseDto.setBoardTypeNm(resolveBoardTypeNm(dto.getBoardTypeCd()));
                    responseDto.setUserName(userName);
                    responseDto.setTitle(dto.getPostSj());
                    responseDto.setContent(dto.getPostCn());
                    responseDto.setInqCnt(dto.getInqCnt());
                    responseDto.setRegDt(dto.getRegDt() != null ? dto.getRegDt().format(formatter) : null);
                    responseDto.setMdfcnDt(dto.getMdfcnDt() != null ? dto.getMdfcnDt().format(formatter) : null);
                    responseDto.setAtchFileId(dto.getAtchFileId() != null ? dto.getAtchFileId().toString() : null);
                    return responseDto;
                })
                .toList();
        return new PageResponse<>(items, totalCount);
    }

    @Override
    public InstructorBoardResponse getInstructorBoardDetail(Long postSn, String instrUserId) {
        InstructorBoardDto original = instructorBoardMapper.selectInstructorBoardDetail(postSn, instrUserId);
        if (original == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String userName = original.getMemberDto() != null ? original.getMemberDto().getUserName() : "";
        InstructorBoardResponse response = InstructorBoardResponse.builder()
                .postSn(original.getPostSn())
                .useYn(original.getUseYn())
                .boardTypeCd(original.getBoardTypeCd())
                .boardTypeNm(resolveBoardTypeNm(original.getBoardTypeCd()))
                .userName(userName)
                .title(original.getPostSj())
                .content(original.getPostCn())
                .inqCnt(original.getInqCnt())
                .regDt(original.getRegDt() != null ? original.getRegDt().format(formatter) : null)
                .mdfcnDt(original.getMdfcnDt() != null ? original.getMdfcnDt().format(formatter) : null)
                .atchFileId(original.getAtchFileId() != null ? original.getAtchFileId().toString() : null)
                .build();

        if ("QNA".equals(original.getBoardTypeCd())) {
            response.setAnswer(instructorBoardMapper.selectInstructorQnaAnswer(original.getPostSn()));
        }
        if (original.getAtchFileId() != null) {
            response.setFiles(fileUploadService.retrieveFilesByGroupId(original.getAtchFileId().intValue()));
        }

        return response;
    }

    @Override
    @Transactional
    public int insertInstructorBoard(InstructorBoardDto instructorBoardDto) {
        int rowcnt = instructorBoardMapper.insertInstructorBoard(instructorBoardDto);
        if (rowcnt > 0) {
            if ("QNA".equals(instructorBoardDto.getBoardTypeCd())) {
                instructorBoardMapper.insertInstructorQna(instructorBoardDto.getPostSn());
            }
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
        dto.setBoardTypeCd("NOTICE");
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
        dto.setBoardTypeCd("QNA");
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

    // ── 공개 강사 게시판 Q&A 답변 ──────────────────────────────────
    @Override
    @Transactional
    public int answerInstructorQna(Long postSn, String answrUserId, String answCn) {
        return instructorBoardMapper.updateInstructorQnaAnswer(postSn, answrUserId, answCn);
    }

    // ── 공개 강사 게시판 (React 프론트용) ──────────────────────────
    @Override
    public PageResponse<InstructorPublicBoardItem> getPublicBoardList(
            String instrUuid, String boardTypeCd, int page, int size) {
        int offset = page * size;
        int total = instructorBoardMapper.selectPublicBoardCount(instrUuid, boardTypeCd);
        List<InstructorPublicBoardItem> items
                = instructorBoardMapper.selectPublicBoardList(instrUuid, boardTypeCd, offset, size);
        return new PageResponse<>(items, total);
    }

    private static String resolveBoardTypeNm(String boardTypeCd) {
        if (boardTypeCd == null) return "";
        try { return BoardType.valueOf(boardTypeCd).getLabel(); } catch (IllegalArgumentException e) { return boardTypeCd; }
    }

    @Override
    @Transactional
    public InstructorPublicBoardDetail getPublicBoardDetail(String instrUuid, Long postSn) {
        InstructorPublicBoardDetail detail
                = instructorBoardMapper.selectPublicBoardDetail(instrUuid, postSn);
        if (detail == null) {
            return null;
        }
        instructorBoardMapper.incrementViewCount(postSn);
        detail.setPrevPost(instructorBoardMapper.selectPrevPost(instrUuid, detail.getBoardTypeCd(), postSn));
        detail.setNextPost(instructorBoardMapper.selectNextPost(instrUuid, detail.getBoardTypeCd(), postSn));
        if ("Y".equals(detail.getHasFile())) {
            detail.setFiles(instructorBoardMapper.selectBoardFiles(postSn));
        }
        return detail;
    }

}
