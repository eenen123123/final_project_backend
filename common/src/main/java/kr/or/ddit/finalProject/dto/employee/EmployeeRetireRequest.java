package kr.or.ddit.finalProject.dto.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeRetireRequest {

    @NotBlank(message = "퇴사 사유는 필수 입니다.")
    @Size(max = 1000, message = "퇴사 사유는 1000자 이하로 입력해야 합니다.")
    private String retmtRsn;
}
