package kr.or.ddit.finalProject.dto.board.req;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EditorPostRequestDto implements Serializable {

    @NotBlank
    private String postSj; // 제목

    @NotNull
    private JsonNode postCn; // TipTap JSONContent (객체로 받아 검증 후 String 직렬화해서 DB 저장)

    @Size(max = 10)
    private List<Long> fileIds; // 이미지 fileId 목록 — 서비스 계층에서 소유자 검증 + CTX_ID 업데이트에 사용
}
