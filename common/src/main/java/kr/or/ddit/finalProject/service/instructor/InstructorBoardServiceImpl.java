package kr.or.ddit.finalProject.service.instructor;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.classroom.ClassroomQnaDto;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.instructor.board.BoardType;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardDto;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorBoardResponse;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorPublicBoardDetail;
import kr.or.ddit.finalProject.dto.instructor.board.InstructorPublicBoardItem;
import kr.or.ddit.finalProject.mapper.instructor.InstructorBoardMapper;
import kr.or.ddit.finalProject.service.file.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 강사 게시판 서비스 구현체.
 * 매퍼 호출 결과를 뷰에 맞는 응답 DTO로 변환하고 파일 조회를 병합한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstructorBoardServiceImpl implements InstructorBoardService {

    /** 목록용 날짜 포맷 (yyyy-MM-dd) */
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 상세용 날짜+시각 포맷 (yyyy-MM-dd HH:mm:ss) */
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final InstructorBoardMapper instructorBoardMapper;
    private final FileUploadService fileUploadService;

    // ── 강사 홈페이지 게시판 ──────────────────────────────────────────

    @Override
    public PageResponse<InstructorBoardResponse> getInstructorBoardList(
            String instrUserId, String keyword, String boardTypeCd, String searchType,
            int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        int totalCount = instructorBoardMapper.selectInstructorBoardCount(instrUserId, keyword, boardTypeCd, searchType);
        List<InstructorBoardDto> rows = instructorBoardMapper.selectInstructorBoardList(
                instrUserId, keyword, boardTypeCd, searchType, offset, pageSize);

        List<InstructorBoardResponse> items = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            InstructorBoardDto dto = rows.get(i);
            String userName = dto.getMemberDto() != null ? dto.getMemberDto().getUserName() : "";
            items.add(InstructorBoardResponse.builder()
                    .postSn(dto.getPostSn())
                    .displayNo(totalCount - offset - i)
                    .useYn(dto.getUseYn())
                    .boardTypeCd(dto.getBoardTypeCd())
                    .boardTypeNm(resolveBoardTypeNm(dto.getBoardTypeCd()))
                    .userName(userName)
                    .title(dto.getPostSj())
                    .inqCnt(dto.getInqCnt())
                    .regDt(dto.getRegDt() != null ? dto.getRegDt().format(DATE_FMT) : null)
                    .mdfcnDt(dto.getMdfcnDt() != null ? dto.getMdfcnDt().format(DATE_FMT) : null)
                    .atchFileId(dto.getAtchFileId() != null ? dto.getAtchFileId().toString() : null)
                    .answYn(dto.getAnswYn())
                    .build());
        }
        return new PageResponse<>(items, totalCount);
    }

    @Override
    public InstructorBoardResponse getInstructorBoardDetail(Long postSn, String instrUserId) {
        InstructorBoardDto dto = instructorBoardMapper.selectInstructorBoardDetail(postSn, instrUserId);
        if (dto == null) {
            return null;
        }
        String userName = dto.getMemberDto() != null ? dto.getMemberDto().getUserName() : "";
        InstructorBoardResponse response = InstructorBoardResponse.builder()
                .postSn(dto.getPostSn())
                .useYn(dto.getUseYn())
                .boardTypeCd(dto.getBoardTypeCd())
                .boardTypeNm(resolveBoardTypeNm(dto.getBoardTypeCd()))
                .userName(userName)
                .title(dto.getPostSj())
                .content(dto.getPostCn())
                .inqCnt(dto.getInqCnt())
                .regDt(dto.getRegDt() != null ? dto.getRegDt().format(DATETIME_FMT) : null)
                .mdfcnDt(dto.getMdfcnDt() != null ? dto.getMdfcnDt().format(DATETIME_FMT) : null)
                .atchFileId(dto.getAtchFileId() != null ? dto.getAtchFileId().toString() : null)
                .build();

        if ("QNA".equals(dto.getBoardTypeCd())) {
            response.setAnswer(instructorBoardMapper.selectInstructorQnaAnswer(dto.getPostSn()));
        }
        if (dto.getAtchFileId() != null) {
            response.setFiles(fileUploadService.retrieveFilesByGroupId(dto.getAtchFileId().intValue()));
        }
        return response;
    }

    @Override
    @Transactional
    public int insertInstructorBoard(InstructorBoardDto dto) {
        int rowcnt = instructorBoardMapper.insertInstructorBoard(dto);
        if (rowcnt > 0) {
            if ("QNA".equals(dto.getBoardTypeCd())) {
                instructorBoardMapper.insertInstructorQna(dto.getPostSn());
            }
            log.info("게시글 등록 성공 : {}", dto);
        } else {
            log.warn("게시글 등록 실패 : {}", dto);
        }
        return rowcnt;
    }

    @Override
    public int updateInstructorBoard(InstructorBoardDto dto) {
        int rowcnt = instructorBoardMapper.updateInstructorBoard(dto);
        if (rowcnt > 0) {
            log.info("게시글 수정 성공 : {}", dto);
        } else {
            log.warn("게시글 수정 실패 : {}", dto);
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

    @Override
    public int hardDeleteInstructorBoard(Long postSn) {
        return instructorBoardMapper.hardDeleteInstructorBoard(postSn);
    }

    @Override
    @Transactional
    public int answerInstructorQna(Long postSn, String answrUserId, String answCn) {
        return instructorBoardMapper.updateInstructorQnaAnswer(postSn, answrUserId, answCn);
    }

    // ── 클래스룸 공지사항 ──────────────────────────────────────────────

    @Override
    public PageResponse<InstructorBoardDto> getClassroomNoticeList(Long classSn, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<InstructorBoardDto> items = instructorBoardMapper.selectClassroomNoticeList(classSn, offset, pageSize);
        int totalCount = instructorBoardMapper.countClassroomNoticeList(classSn);
        return new PageResponse<>(items, totalCount);
    }

    @Override
    public InstructorBoardDto getClassroomNoticeDetail(Long postSn, Long classSn) {
        InstructorBoardDto dto = instructorBoardMapper.selectClassroomNoticeDetail(postSn, classSn);
        if (dto != null && dto.getAtchFileId() != null) {
            dto.setAttachedFiles(fileUploadService.retrieveFilesByGroupId(dto.getAtchFileId().intValue()));
        }
        return dto;
    }

    @Override
    public int insertClassroomNotice(InstructorBoardDto dto) {
        dto.setBoardTypeCd("NOTICE");
        return instructorBoardMapper.insertClassroomNotice(dto);
    }

    @Override
    @Transactional
    public int updateClassroomNotice(InstructorBoardDto dto) {
        return instructorBoardMapper.updateClassroomNotice(dto);
    }

    @Override
    public int deleteClassroomNotice(Long postSn, Long classSn) {
        return instructorBoardMapper.deleteClassroomNotice(postSn, classSn);
    }

    // ── 클래스룸 자료실 ───────────────────────────────────────────────

    @Override
    public PageResponse<InstructorBoardDto> getClassroomDataroomList(Long classSn, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<InstructorBoardDto> items = instructorBoardMapper.selectClassroomDataroomList(classSn, offset, pageSize);
        int totalCount = instructorBoardMapper.countClassroomDataroomList(classSn);
        return new PageResponse<>(items, totalCount);
    }

    @Override
    public InstructorBoardDto getClassroomDataroomDetail(Long postSn, Long classSn) {
        InstructorBoardDto dto = instructorBoardMapper.selectClassroomDataroomDetail(postSn, classSn);
        if (dto != null && dto.getAtchFileId() != null) {
            dto.setAttachedFiles(fileUploadService.retrieveFilesByGroupId(dto.getAtchFileId().intValue()));
        }
        return dto;
    }

    @Override
    public int insertClassroomDataroom(InstructorBoardDto dto) {
        dto.setBoardTypeCd("DATAROOM");
        return instructorBoardMapper.insertClassroomDataroom(dto);
    }

    @Override
    @Transactional
    public int updateClassroomDataroom(InstructorBoardDto dto) {
        return instructorBoardMapper.updateClassroomDataroom(dto);
    }

    @Override
    public int deleteClassroomDataroom(Long postSn, Long classSn) {
        return instructorBoardMapper.deleteClassroomDataroom(postSn, classSn);
    }

    // ── 클래스룸 Q&A ──────────────────────────────────────────────────

    @Override
    public PageResponse<ClassroomQnaDto> getClassroomQnaList(Long classSn, int page, int pageSize, String writerUserId) {
        int offset = (page - 1) * pageSize;
        List<ClassroomQnaDto> items = instructorBoardMapper.selectClassroomQnaList(classSn, offset, pageSize, writerUserId);
        int totalCount = instructorBoardMapper.countClassroomQnaList(classSn, writerUserId);
        return new PageResponse<>(items, totalCount);
    }

    @Override
    public ClassroomQnaDto getClassroomQnaDetail(Long postSn, Long classSn) {
        return instructorBoardMapper.selectClassroomQnaDetail(postSn, classSn);
    }

    @Override
    @Transactional
    public void insertClassroomQna(InstructorBoardDto dto) {
        dto.setBoardTypeCd("QNA");
        instructorBoardMapper.insertClassroomQnaBoard(dto);
        instructorBoardMapper.insertClassroomQnaChild(dto.getPostSn());
    }

    @Override
    @Transactional
    public void updateClassroomQna(Long postSn, Long classSn, String wrtrUserId, String postSj, String postCn) {
        ClassroomQnaDto existing = instructorBoardMapper.selectClassroomQnaDetail(postSn, classSn);
        if (existing == null) throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        if (!wrtrUserId.equals(existing.getWrtrUserId())) throw new SecurityException("수정 권한이 없습니다.");
        instructorBoardMapper.updateClassroomQna(postSn, classSn, postSj, postCn);
    }

    @Override
    public void answerClassroomQna(Long postSn, String answrUserId, String answCn) {
        instructorBoardMapper.updateClassroomQnaAnswer(postSn, answrUserId, answCn);
    }

    @Override
    public int getUnansweredQnaCount(Long classSn) {
        return instructorBoardMapper.selectUnansweredQnaCount(classSn);
    }

    @Override
    public List<kr.or.ddit.finalProject.dto.classroom.ClassroomQnaDto> getRecentQnaByStudent(Long classSn, String userId, int limit) {
        return instructorBoardMapper.selectRecentQnaByStudent(classSn, userId, limit);
    }

    // ── 공개 강사 게시판 (React 프론트 전용) ─────────────────────────

    @Override
    public PageResponse<InstructorPublicBoardItem> getPublicBoardList(
            String instrUuid, String boardTypeCd, int pageIndex, int size) {
        int offset = pageIndex * size;
        int total = instructorBoardMapper.selectPublicBoardCount(instrUuid, boardTypeCd);
        List<InstructorPublicBoardItem> items =
                instructorBoardMapper.selectPublicBoardList(instrUuid, boardTypeCd, offset, size);
        return new PageResponse<>(items, total);
    }

    @Override
    @Transactional
    public InstructorPublicBoardDetail getPublicBoardDetail(String instrUuid, Long postSn) {
        InstructorPublicBoardDetail detail =
                instructorBoardMapper.selectPublicBoardDetail(instrUuid, postSn);
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

    @Override
    public void incrementViewCount(Long postSn) {
        instructorBoardMapper.incrementViewCount(postSn);
    }

    // ── 내부 유틸 ─────────────────────────────────────────────────────

    /** BoardType enum에서 한글 레이블을 반환한다. 알 수 없는 코드면 코드 그대로 반환. */
    private static String resolveBoardTypeNm(String boardTypeCd) {
        if (boardTypeCd == null) return "";
        try {
            return BoardType.valueOf(boardTypeCd).getLabel();
        } catch (IllegalArgumentException e) {
            return boardTypeCd;
        }
    }
}
