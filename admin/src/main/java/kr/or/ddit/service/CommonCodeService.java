package kr.or.ddit.service;

import java.util.List;

import kr.or.ddit.finalProject.dto.code.ComClDto;
import kr.or.ddit.finalProject.dto.common.CommonCodeDto;

public interface CommonCodeService {
    List<ComClDto> getGroups();
    void createGroup(ComClDto dto, String adminId);
    void updateGroup(ComClDto dto, String adminId);
    void deleteGroup(String clCode);

    List<CommonCodeDto> getAllCodes(String clCode);
    void createCode(CommonCodeDto dto, String adminId);
    void updateCode(CommonCodeDto dto, String adminId);
    void deleteCode(String clCode, String comCd);
}
