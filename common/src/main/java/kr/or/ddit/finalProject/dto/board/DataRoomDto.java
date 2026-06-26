package kr.or.ddit.finalProject.dto.board;

import java.util.List;
import kr.or.ddit.finalProject.dto.file.FileDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DataRoomDto extends BoardDto {

    private String dataCtg;    // 자료실 카테고리
    private Long expsOrd;      // 게시글 정렬순서
    private String accsLmtCd;  // 접근 제한코드 (전체공개 01, 회원전용 02)

    // 공통코드 조인 필드
    private String dataCtgNm;
    private String accsLmtNm;

    // 파일 첨부 (입력용 - 최대 5개)
    private transient List<MultipartFile> attachFiles;

    // 첨부파일 목록 (조회용)
    private List<FileDto> files;
}
