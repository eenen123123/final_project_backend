package kr.or.ddit.finalProject.dto.course;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseContentDto implements Serializable {

    private Long contSn; // 기본키(PK) · 시퀀스
    private Long courseSn;
    private String lectSj; // 예: 1차시. 오리엔테이션
    private String lectTypeCd; // COM_CD 공통코드 참조
    private String videoUrlAddr;
    private Integer videoPlayTimeCnt; // 초(second) 단위 정수 저장 권장
    private Long sortOrd; // 강의 목록 노출 순서
    private String atchFileId; // 공통첨부파일분류
    private String rgtrId;
    private String lastMdfrId;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
}
