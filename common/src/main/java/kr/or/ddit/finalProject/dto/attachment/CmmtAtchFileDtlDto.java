package kr.or.ddit.finalProject.dto.attachment;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmmtAtchFileDtlDto implements Serializable {

    private Long atchFileDtlSn; // 기본키(PK) · 시퀀스
    private Long atchFileId;
    private String orgnFileNm;
    private String savePathNm;
    private String saveFileNm;
    private String fileExtNm; // jpg·png·pdf·docx 등
    private Long fileSizeCnt; // (Byte 단위)
    private byte[] fileCn; // 바이너리 또는 텍스트
    private LocalDateTime regDt;
    private String rgtrId;
    private String delYn; // Y : 삭제 / N : 정상
    private LocalDateTime delDt;
    private String delUserId;
    private Long dwnldCnt; // 파일 다운로드 횟수 누적
}