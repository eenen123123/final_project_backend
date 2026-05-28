package kr.or.ddit.finalProject.mapper.common;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.common.CommonCodeDto;

@Mapper
public interface CommonCodeMapper {

    List<CommonCodeDto> selectByClCode(@Param("clCode") String clCode);
}
