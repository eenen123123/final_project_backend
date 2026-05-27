package kr.or.ddit.finalProject.dto.post;

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
public class PostMasterDto implements Serializable {

    private Long ntceSn; // 기본키(PK) · 자동증가
    private String ntceSj;
    private String ntceCn;
    private LocalDateTime ntceSndDt;
    private String ntceTypeCd; // COM_CD 공통코드 참조
}
