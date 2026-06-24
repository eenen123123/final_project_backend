package kr.or.ddit.finalProject.dto.instructor.board;

import lombok.Data;

/**
 * 강사 공개 게시판 첨부파일 항목 DTO (React 프론트 전용).
 * selectBoardFiles 쿼리 결과를 담으며,
 * InstructorPublicBoardDetail.files 리스트로 포함된다.
 */
@Data
public class InstructorBoardFileItem {

    /** 첨부파일 상세 일련번호 (CMMT_ATCH_FILE_DTL.ATCH_FILE_DTL_SN) */
    private Long atchFileDtlSn;

    /** 원본 파일명 */
    private String fileName;

    /** 파일 접근 URL (Cloudinary 또는 파일 서버 경로) */
    private String fileUrl;
}
