package kr.or.ddit.finalProject.mapper.log;

import org.apache.ibatis.annotations.Mapper;

import kr.or.ddit.finalProject.dto.log.SystemErrorLogDto;

@Mapper
public interface SystemErrorLogMapper {
    void insertSystemErrorLog(SystemErrorLogDto dto);
}
