package kr.or.ddit.finalProject.controller;

import java.io.Serializable;
import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.member.AdminMemberDto;
import kr.or.ddit.finalProject.mapper.MemberMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// 개발을 위해 임시로 만든 컨트롤러, 실제 서비스에서는 사용하지 않음

@Slf4j
@RestController
@RequiredArgsConstructor
@Profile("dev")
public class AccountControllerForDev {
    private final MemberMapper memberMapper;

    @Getter
    @Setter
    private class AccountInfo implements Serializable {
        private String userId;
        private String userName;
        private String userRole;
        private String department;
        private String jobGrade;

        public AccountInfo(AdminMemberDto member) {
            this.userId = member.getUserId();
            this.userName = member.getUserName();
            this.userRole = member.getUserRole();
            if (member.getEmployeeInfo() != null) {
                this.department = member.getEmployeeInfo().getDeptNm();
                this.jobGrade = member.getEmployeeInfo().getJbgrNm();
            } else {
                this.department = "N/A";
                this.jobGrade = "N/A";
            }
        }
    }

    private List<String> userIds = List.of("testuser01", // 원장

            "testuser02", // 행정팀장
            "testuser23", // 선임행정원
            "testuser25", // 행정팀 직원

            "testuser03", // 총괄PD
            "testuser66", // 메인PD
            "testuser07", // 서브PD

            "testuser04" // 수석강사
            , "testuser05" // 전임강사
            , "testuser19" // 조교

            , "testuser101" // 일반 회원
            , "testuser107" // 일반 회원
            , "testuser122" // 일반 회원

            , "testuser104" // 21번 ClassRoom의 학생

            , "testuser103" // (학생 111의 학부모)
            , "testuser105" // (학생 112의 학부모)
            , "testuser163" // (학생 116의 학부모)
    );

    public List<AccountInfo> getAllAccounts() {
        return userIds.stream().map(id -> {
            AdminMemberDto adminMemberDto = memberMapper.getAdminUserById(id);
            if (adminMemberDto == null) {
                adminMemberDto = memberMapper.findAdminByUserId(id).orElse(null);
            }
            return adminMemberDto;
        }).map(member -> {

            AccountInfo accountInfo = new AccountInfo(member);
            if (accountInfo.getUserId().equals("testuser104")) {
                accountInfo.setDepartment("ClassRoom 21");

            } else if (accountInfo.getUserId().equals("testuser103")) {
                accountInfo.setDepartment("111의 학부모");
            } else if (accountInfo.getUserId().equals("testuser105")) {
                accountInfo.setDepartment("112의 학부모");
            } else if (accountInfo.getUserId().equals("testuser163")) {
                accountInfo.setDepartment("116의 학부모");
            }
            return accountInfo;

        }).toList();
    }

    @GetMapping("/api/temp/accounts")
    public ResponseEntity<List<AccountInfo>> getAccounts() {
        return ResponseEntity.ok(getAllAccounts());
    }

}
