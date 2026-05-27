package kr.or.ddit.fix;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.mapper.EmployeeMapper;
import kr.or.ddit.finalProject.mapper.MemberMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class MemberFix {
    @Autowired
    MemberMapper memberMapper;

    @Autowired
    EmployeeMapper employeeMapper;

    @Test
    void memberFix() {


        MemberDto memberDto = memberMapper.findByUserId("testuser02").orElse(null);

        EmployeeInfoDto employeeInfoDto = EmployeeInfoDto.builder().userId(memberDto.getUserId())
                .deptCd("D100").jbgrCd("A001").emplStatCd("01").build();
        int result = employeeMapper.insertEmployeeInfo(employeeInfoDto);
        log.info("Inserted employee info result: {}", result);

    }

    @Test
    void test2() {
        List<MemberDto> memberList = memberMapper.findAllMembers();
        for (MemberDto member : memberList) {
            if (member.getUserId().equals("testuser01") || member.getUserId().equals("testuser02")
                    || member.getUserId().equals("testuser03")
                    || member.getUserId().equals("testuser04"))
                continue;
            int randomNumZeroToTwo = (int) (Math.random() * 3);
            log.info("Random number between 0 and 2: {}", randomNumZeroToTwo);



            EmployeeInfoDto employeeInfoDto =
                    EmployeeInfoDto.builder().userId(member.getUserId()).emplStatCd("01").build();
            if (randomNumZeroToTwo == 0) {
                employeeInfoDto.setDeptCd("D100");
                employeeInfoDto.setJbgrCd("A004");
            } else if (randomNumZeroToTwo == 1) {
                employeeInfoDto.setDeptCd("D200");
                employeeInfoDto.setJbgrCd("P004");
            } else {
                employeeInfoDto.setDeptCd("D300");
                employeeInfoDto.setJbgrCd("T004");
            }
            int result = employeeMapper.insertEmployeeInfo(employeeInfoDto);
        }
    }
}
