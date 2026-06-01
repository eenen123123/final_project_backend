package kr.or.ddit.controller.board.dataroom;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kr.or.ddit.finalProject.dto.board.DataRoomDto;
import kr.or.ddit.finalProject.service.board.dataroom.DataRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/dataroom")
@RequiredArgsConstructor
public class DataRoomApiController {

    private final DataRoomService dataRoomService;

    // 자료실 목록 조회
    // GET /api/dataroom?dataCtg=01
    @GetMapping
    public ResponseEntity<List<DataRoomDto>> getDataRoomList(
            @RequestParam(required = false) String dataCtg) {
        return ResponseEntity.ok(dataRoomService.getDataRoomList(dataCtg));
    }

    // 자료실 단건 조회
    // GET /api/dataroom/{postSn}
    @GetMapping("/{postSn}")
    public ResponseEntity<DataRoomDto> getDataRoomById(@PathVariable Long postSn) {
        return ResponseEntity.ok(dataRoomService.getDataRoomById(postSn));
    }
}
