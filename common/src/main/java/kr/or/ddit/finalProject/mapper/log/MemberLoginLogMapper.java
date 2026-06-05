package kr.or.ddit.finalProject.mapper.log;

import org.apache.ibatis.annotations.Mapper;
import kr.or.ddit.finalProject.dto.log.MemberLoginLogDto;

@Mapper
public interface MemberLoginLogMapper {
    void insertMemberLoginLog(MemberLoginLogDto dto);
    int updateMemberLogout(String userId);
    void closeAllOpenSessions();
}
