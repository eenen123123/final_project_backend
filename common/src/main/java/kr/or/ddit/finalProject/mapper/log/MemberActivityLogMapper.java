package kr.or.ddit.finalProject.mapper.log;

import org.apache.ibatis.annotations.Mapper;

import kr.or.ddit.finalProject.dto.log.MemberActivityLogDto;

@Mapper
public interface MemberActivityLogMapper {
    void insertMemberActivityLog(MemberActivityLogDto dto);
}
