package kr.or.ddit.finalProject.dto.file;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * [변경 이유] @NoArgsConstructor, @AllArgsConstructor 추가
 *
 * @Data + @Builder 조합만 사용할 경우, Lombok이 생성하는 전체 인수 생성자(all-args constructor)를
 * MyBatis가 결과 매핑 시 사용하려 시도합니다.
 * 강좌 자료 관리 기능에서 selectFilesByGroupId 쿼리를 추가하면서,
 * SELECT 컬럼 수(11개)와 생성자 파라미터 수(14개)가 달라
 * java.lang.IndexOutOfBoundsException이 발생했습니다.
 *
 * MyBatis는 기본적으로 no-args constructor + setter 방식으로 결과를 매핑하므로,
 * @NoArgsConstructor를 명시해 이 동작을 보장합니다.
 * @Builder와 함께 사용할 때는 @AllArgsConstructor도 함께 선언해야 합니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
