package kr.or.ddit.mapper;

import kr.or.ddit.dto.AuditLogDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper {
    void insertAuditLog(AuditLogDto dto);
}
