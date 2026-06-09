package kr.or.ddit.finalProject.dto.student;

import java.io.Serializable;
import java.time.LocalDateTime;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import lombok.Data;

@Data
public class StudentInfoDto implements Serializable {

    private String userId;
    private String userName;
    private String userTelno;
    private LocalDateTime userBirth;
    private String userZip;
    private String userAddr;
    private String userDaddr;


    public StudentInfoDto(MemberDto memberDto) {
        this.userId = memberDto.getUserId();
        this.userName = memberDto.getUserName();
        this.userTelno = memberDto.getUserTelno();
        this.userBirth = memberDto.getUserBrdt().atStartOfDay();
        this.userZip = memberDto.getUserZip();
        this.userAddr = memberDto.getUserAddr();
        this.userDaddr = memberDto.getUserDaddr();
    }

}
