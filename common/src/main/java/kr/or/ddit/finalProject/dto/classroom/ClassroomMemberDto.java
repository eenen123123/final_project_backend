package kr.or.ddit.finalProject.dto.classroom;

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
public class ClassroomMemberDto implements Serializable {

    private Long enrlSn;
    private Long classSn;
    private String userId;
    private String enrlStatCd; // 01=수강중, 02=수강완료, 03=중도탈퇴, 04=등록취소
    private String rgtrId;
    private LocalDateTime regDt;
    private String lastMdfrId;
    private LocalDateTime mdfcnDt;

}
