package kr.or.ddit.finalProject.dto.file;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileDto implements Serializable {

    private Integer atchFileDtlSn; // 첨부파일 상세 ID PK sequence
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
}
