package kr.or.ddit.finalProject.dto.classroom;

import java.util.List;
import kr.or.ddit.finalProject.dto.file.FileDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudentAssignmentDetail {
    private Long asgmtSn;
    private String asgmtNm;
    private String asgmtCn;
    private String dueDt;
    private boolean submitted;
    private String sbmtCn;
    private Double score;
    private String feedbackCn;
    private String resubmitYn;
    private List<FileDto> attachedFiles;
}
