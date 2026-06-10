package kr.or.ddit.finalProject.dto.code;

import lombok.Data;

@Data
public class ComClDto {
    private String clCode;
    private String clCdNm;
    private String clCdExpln;
    private String useYn;
    private String rgtrId;
    private String lastMdfrId;
    private String regDt;
    private String mdfcnDt;
    private int codeCount;
}
