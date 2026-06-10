package kr.or.ddit.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.code.ComClDto;
import kr.or.ddit.finalProject.dto.common.CommonCodeDto;
import kr.or.ddit.finalProject.mapper.common.CommonCodeMapper;
import kr.or.ddit.service.CommonCodeService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonCodeServiceImpl implements CommonCodeService {

    private final CommonCodeMapper mapper;

    @Override
    public List<ComClDto> getGroups() {
        return mapper.selectAllGroups();
    }

    @Override
    @Transactional
    public void createGroup(ComClDto dto, String adminId) {
        dto.setRgtrId(adminId);
        mapper.insertGroup(dto);
    }

    @Override
    @Transactional
    public void updateGroup(ComClDto dto, String adminId) {
        dto.setLastMdfrId(adminId);
        mapper.updateGroup(dto);
    }

    @Override
    @Transactional
    public void deleteGroup(String clCode) {
        mapper.deleteCodesByGroup(clCode);
        mapper.deleteGroup(clCode);
    }

    @Override
    public List<CommonCodeDto> getAllCodes(String clCode) {
        return mapper.selectAllByClCode(clCode);
    }

    @Override
    @Transactional
    public void createCode(CommonCodeDto dto, String adminId) {
        dto.setRgtrId(adminId);
        mapper.insertCode(dto);
    }

    @Override
    @Transactional
    public void updateCode(CommonCodeDto dto, String adminId) {
        dto.setLastMdfrId(adminId);
        mapper.updateCode(dto);
    }

    @Override
    @Transactional
    public void deleteCode(String clCode, String comCd) {
        mapper.deleteCode(clCode, comCd);
    }
}
