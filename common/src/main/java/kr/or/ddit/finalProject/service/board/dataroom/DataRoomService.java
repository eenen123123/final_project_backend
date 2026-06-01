package kr.or.ddit.finalProject.service.board.dataroom;

import java.util.List;
import kr.or.ddit.finalProject.dto.board.DataRoomDto;
import org.springframework.web.multipart.MultipartFile;

public interface DataRoomService {

    /**
     * 자료실 목록 조회
     *
     * @param dataCtg 자료실 카테고리 코드 (CL_CODE: 106), null이면 전체 조회
     * @return 자료실 목록
     */
    List<DataRoomDto> getDataRoomList(String dataCtg);

    /**
     * 자료실 단건 조회
     *
     * @param postSn 자료실 PK
     * @return 자료실 상세 정보
     */
    DataRoomDto getDataRoomById(Long postSn);

    /**
     * 자료실 등록
     *
     * @param dataRoomDto 등록할 자료실 정보
     * @param file 첨부파일 (없으면 null)
     */
    void createDataRoom(DataRoomDto dataRoomDto, MultipartFile file);

    /**
     * 자료실 수정
     *
     * @param dataRoomDto 수정할 자료실 정보
     * @param file 첨부파일 (없으면 null)
     */
    void updateDataRoom(DataRoomDto dataRoomDto, MultipartFile file);

    /**
     * 자료실 삭제
     *
     * @param postSn 삭제할 자료실 PK
     */
    void deleteDataRoom(Long postSn);

}
