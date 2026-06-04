package kr.or.ddit.controller.board.dataroom;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.board.DataRoomDto;
import kr.or.ddit.finalProject.dto.board.req.DataRoomSearchCondition;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.board.dataroom.DataRoomService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dataroom")
@RequiredArgsConstructor
public class DataRoomController {

    private final DataRoomService dataRoomService;

    // GET /api/dataroom/paged?page=1&size=10&keyword=xxx&dataCtg=01
    @GetMapping("/paged")
    public ResponseEntity<PageResponse<DataRoomDto>> getDataRoomListPaged(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String dataCtg) {
        PaginationInfo<DataRoomSearchCondition> paginationInfo = new PaginationInfo<>(size, 5, page);
        paginationInfo.setDetailCondition(new DataRoomSearchCondition(keyword, dataCtg));
        return ResponseEntity.ok(dataRoomService.getList(paginationInfo));
    }

    // GET /api/dataroom/{postSn}
    @GetMapping("/{postSn}")
    public ResponseEntity<DataRoomDto> getDataRoomById(@PathVariable Long postSn) {
        return ResponseEntity.ok(dataRoomService.getById(postSn, null));
    }
}
