package kr.or.ddit.finalProject.dto.permission;

import lombok.Data;

@Data
public class MenuPermissionDto {
    private String menuCd;
    private String jobGrade;
    private String allowed; // "Y" or "N"
}
