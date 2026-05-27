package kr.or.ddit.finalProject.dto.board;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataRoomDto implements Serializable {

    private Long postSn; // 기본키(PK) · 시퀀스
    private String dataCtg; // 강의노트, 참고서적 등
    private Long expsOrd;
    private String accsLmtCd; // 전체공개, 수강생 전용 등
}
