package kr.or.ddit.finalProject.dto.log;

import lombok.Data;

@Data
public class AdminAuditLogDto {
    private Integer auditId;
    private String traceId;
    private String adminId;
    private String memberIp;
    private String httpMethod;
    private String requestUri;
    private String requestParams;
    private Integer statusCode;
    private String createdAt;
}
