package kr.or.ddit.finalProject.service.board.dataroom;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import kr.or.ddit.finalProject.dto.board.DataRoomDto;
import kr.or.ddit.finalProject.dto.board.req.DataRoomSearchCondition;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.file.FileCtxType;
import kr.or.ddit.finalProject.mapper.FileMapper;
import kr.or.ddit.finalProject.mapper.board.DataRoomMapper;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.file.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class DataRoomServiceImpl implements DataRoomService {

    private final DataRoomMapper dataRoomMapper;
    private final FileUploadService fileUploadService;
    private final FileMapper fileMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DataRoomDto> getList(PaginationInfo<DataRoomSearchCondition> paginationInfo) {
        List<DataRoomDto> items = dataRoomMapper.findDataRoomListPaged(paginationInfo);
        int totalCount = dataRoomMapper.countDataRoomList(paginationInfo);
        return new PageResponse<>(items, totalCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataRoomDto> getAll(String dataCtg) {
        return dataRoomMapper.findDataRoomList(dataCtg);
    }

    @Override
    @Transactional(readOnly = true)
    public DataRoomDto getById(Long postSn, Authentication authentication) {
        DataRoomDto dto = dataRoomMapper.findDataRoomById(postSn);
        if (dto != null) {
            dto.setFiles(fileMapper.selectFilesByCtx(FileCtxType.POST.name(), String.valueOf(postSn)));
        }
        return dto;
    }

    @Override
    @Transactional
    public void create(DataRoomDto dto, Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "admin";
        dto.setWrtrUserId(userId);
        dataRoomMapper.insertBoard(dto);
        dataRoomMapper.insertDataRoom(dto);

        // 파일 업로드 (최대 5개)
        List<MultipartFile> files = dto.getAttachFiles();
        if (files != null) {
            files.stream()
                .filter(f -> f != null && !f.isEmpty())
                .limit(5)
                .forEach(f -> fileUploadService.uploadFile(f, userId, FileCtxType.POST, String.valueOf(dto.getPostSn())));
        }
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
