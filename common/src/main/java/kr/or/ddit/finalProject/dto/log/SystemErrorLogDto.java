package kr.or.ddit.finalProject.dto.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemErrorLogDto {
    private Integer errorId;
    private String traceId;
    private String errorCode;
    private String requestUri;
    private String errorMessage;
    private String createdAt;
}
