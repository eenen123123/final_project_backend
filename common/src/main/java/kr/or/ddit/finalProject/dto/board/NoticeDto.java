package kr.or.ddit.finalProject.dto.board;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NoticeDto extends BoardDto {

    private String noticeTypeCd; // 공지 유형 (CL_CODE: 103)

    // 공통코드 조인 필드
    private String noticeTypeNm;
}
