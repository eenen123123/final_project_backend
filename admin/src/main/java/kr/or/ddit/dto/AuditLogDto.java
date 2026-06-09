package kr.or.ddit.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditLogDto {
    private String traceId;
    private String adminId;
    private String memberIp;
    private String httpMethod;
    private String requestUri;
    private String requestParams;
    private int statusCode;
}
