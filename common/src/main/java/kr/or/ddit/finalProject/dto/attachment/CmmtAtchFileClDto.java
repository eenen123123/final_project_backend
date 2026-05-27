package kr.or.ddit.finalProject.dto.attachment;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmmtAtchFileClDto implements Serializable {

    private Long atchFileId;
    private LocalDateTime cretDt;
    private String useYn; // Y : 여(예) / N : 부(아니요)
}
