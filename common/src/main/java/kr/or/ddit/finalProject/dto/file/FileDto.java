package kr.or.ddit.finalProject.dto.file;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileDto implements Serializable {

    private Integer atchFileDtlSn; // 첨부파일 상세 ID PK sequence
    private Long fileServerId; // 파일 서버 ID (파일서버 DB PK)
    private Integer atchFileId; // 첨부파일 ID
    private String orgnFileNm; // 원본 파일명
    private String savePathNm; // 저장 경로
    private String saveFileNm; // 저장 파일명
    private String fileExtNm; // 파일 확장자
    private Long fileSizeCnt; // 파일 사이즈
    private String fileCn; // 파일 내용
    private String regDt; // 최초 등록 시점
    private String rgtrId; // 최초 등록자 ID 
    private String delYn; // 삭제 여부
    private String delDt; // 삭제 시점
    private String delUserId; // 삭제자 ID
    private Integer dwnldCnt; // 다운로드 횟수

    private FileCtxType fileCtxType; // 파일 컨텍스트 타입
    private Long fileCtxId; // 파일 컨텍스트 ID (예: 강의 ID, 채팅방 ID 등)
}
