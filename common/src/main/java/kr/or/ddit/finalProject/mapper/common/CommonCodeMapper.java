package kr.or.ddit.finalProject.mapper.common;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.code.ComClDto;
import kr.or.ddit.finalProject.dto.common.CommonCodeDto;

@Mapper
public interface CommonCodeMapper {

    // ── 드롭다운 로딩 (USE_YN='Y' 만) ────────────────────────────
    List<CommonCodeDto> selectByClCode(@Param("clCode") String clCode);

    // ── 분류(COM_CL) 관리 ─────────────────────────────────────────
    List<ComClDto> selectAllGroups();
    int insertGroup(ComClDto dto);
    int updateGroup(ComClDto dto);
    int deleteGroup(@Param("clCode") String clCode);

    // ── 코드(COM_CD) 관리 ─────────────────────────────────────────
    List<CommonCodeDto> selectAllByClCode(@Param("clCode") String clCode);
    int insertCode(CommonCodeDto dto);
    int updateCode(CommonCodeDto dto);
    int deleteCode(@Param("clCode") String clCode, @Param("comCd") String comCd);
    int deleteCodesByGroup(@Param("clCode") String clCode);
}
