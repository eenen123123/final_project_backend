package kr.or.ddit.finalProject.service.board.dataroom;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import kr.or.ddit.finalProject.dto.board.DataRoomDto;
import kr.or.ddit.finalProject.dto.file.FileDto;
import kr.or.ddit.finalProject.mapper.board.DataRoomMapper;
import kr.or.ddit.finalProject.service.file.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class DataRoomServiceImpl implements DataRoomService {

    private final DataRoomMapper dataRoomMapper;
    private final FileUploadService fileUploadService;

    @Override
    @Transactional(readOnly = true)
    public List<DataRoomDto> getDataRoomList(String dataCtg) {
        return dataRoomMapper.findDataRoomList(dataCtg);
    }

    @Override
    @Transactional(readOnly = true)
    public DataRoomDto getDataRoomById(Long postSn) {
        return dataRoomMapper.findDataRoomById(postSn);
    }

    @Override
    @Transactional
    public void createDataRoom(DataRoomDto dataRoomDto, MultipartFile file) {
        // 1. 파일 첨부 있으면 업로드
        if (file != null && !file.isEmpty()) {
            FileDto fileDto = fileUploadService.uploadFile(file, dataRoomDto.getWrtrUserId());
            dataRoomDto.setAtchFileId(fileDto.getAtchFileDtlSn());
            dataRoomDto.setOrgnFileNm(fileDto.getOrgnFileNm());
            dataRoomDto.setSavePathNm(fileDto.getSavePathNm());
            dataRoomDto.setFileExtNm(fileDto.getFileExtNm());
            dataRoomDto.setFileSizeCnt(fileDto.getFileSizeCnt());
        }

        // 2. BOARD INSERT → postSn 채번
        dataRoomMapper.insertBoard(dataRoomDto);

        // 3. DATA_ROOM INSERT
        dataRoomMapper.insertDataRoom(dataRoomDto);

        log.info("자료실 등록 완료 : {}", dataRoomDto);
    }

    @Override
    @Transactional
    public void updateDataRoom(DataRoomDto dataRoomDto, MultipartFile file) {
        // 1. 새 파일 첨부 있으면 업로드
        if (file != null && !file.isEmpty()) {
            FileDto fileDto = fileUploadService.uploadFile(file, dataRoomDto.getWrtrUserId());
            dataRoomDto.setAtchFileId(fileDto.getAtchFileDtlSn());
            dataRoomDto.setOrgnFileNm(fileDto.getOrgnFileNm());
            dataRoomDto.setSavePathNm(fileDto.getSavePathNm());
            dataRoomDto.setFileExtNm(fileDto.getFileExtNm());
            dataRoomDto.setFileSizeCnt(fileDto.getFileSizeCnt());
        }

        // 2. BOARD UPDATE
        dataRoomMapper.updateBoard(dataRoomDto);

        // 3. DATA_ROOM UPDATE
        dataRoomMapper.updateDataRoom(dataRoomDto);

        log.info("자료실 수정 완료 : {}", dataRoomDto);
    }

    @Override
    @Transactional
    public void deleteDataRoom(Long postSn) {
        // 1. DATA_ROOM DELETE (FK 때문에 먼저)
        dataRoomMapper.deleteDataRoom(postSn);

        // 2. BOARD DELETE
        dataRoomMapper.deleteBoard(postSn);

        log.info("자료실 삭제 완료 : postSn={}", postSn);
    }

}
