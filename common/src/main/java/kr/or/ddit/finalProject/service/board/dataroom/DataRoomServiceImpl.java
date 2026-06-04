package kr.or.ddit.finalProject.service.board.dataroom;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.board.DataRoomDto;
import kr.or.ddit.finalProject.dto.board.req.DataRoomSearchCondition;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.mapper.board.DataRoomMapper;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class DataRoomServiceImpl implements DataRoomService {

    private final DataRoomMapper dataRoomMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DataRoomDto> getList(PaginationInfo<DataRoomSearchCondition> paginationInfo) {
        List<DataRoomDto> items = dataRoomMapper.findDataRoomListPaged(paginationInfo);
        int totalCount = dataRoomMapper.countDataRoomList(paginationInfo);
        return new PageResponse<>(items, totalCount);
    }

    @Override
    @Transactional(readOnly = true)
    public DataRoomDto getById(Long postSn, Authentication authentication) {
        return dataRoomMapper.findDataRoomById(postSn);
    }

    @Override
    @Transactional
    public void create(DataRoomDto dto, Authentication authentication) {
        if (authentication != null) {
            dto.setWrtrUserId(authentication.getName());
        }
        dataRoomMapper.insertBoard(dto);
        dataRoomMapper.insertDataRoom(dto);
    }

    @Override
    @Transactional
    public void update(DataRoomDto dto) {
        dataRoomMapper.updateBoard(dto);
        dataRoomMapper.updateDataRoom(dto);
    }

    @Override
    @Transactional
    public void delete(Long postSn) {
        dataRoomMapper.deleteDataRoom(postSn);
        dataRoomMapper.deleteBoard(postSn);
    }
}
