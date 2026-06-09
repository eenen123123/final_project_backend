package kr.or.ddit.finalProject.service.member;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.dto.student.StudentDto;
import kr.or.ddit.finalProject.dto.user.ParentJoinLinkDto;
import kr.or.ddit.finalProject.dto.user.SignupRequestRecord;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.MemberMapper;
import kr.or.ddit.finalProject.mapper.ParentJoinMapper;
import kr.or.ddit.finalProject.mapper.StaffMapper;
import kr.or.ddit.finalProject.service.sms.SmsService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParentService {


    private final ParentJoinMapper parentJoinMapper;
    private final StaffMapper staffMapper;
    private final SmsService smsService;
    private final MemberMapper memberMapper;

    public void sendParentJoinLink(String phoneNumber, String baseUrl, String studentId) {

        // 학생 id 검증 로직
        staffMapper.selectStudent(studentId)
                .orElseThrow(() -> new FinalProjectException(ErrorCode.STUDENT_NOT_FOUND));


        // 이전에 생성된 링크가 있다면 삭제

        List<ParentJoinLinkDto> existingLinks = parentJoinMapper.findByStudentId(studentId);
        if (existingLinks != null && !existingLinks.isEmpty()) {
            existingLinks.forEach(link -> parentJoinMapper.deleteParentJoinLink(link.getLinkId()));
        }

        String restUrl = baseUrl.replace(":8080", ":9001");
        String token = java.util.UUID.randomUUID().toString();
        String joinLink = restUrl + "/parent/join?token=" + token;
        String text = "[HERMES 학원] 학부모 회원가입 링크입니다.\n" + joinLink;



        ParentJoinLinkDto joinLinkDto = new ParentJoinLinkDto();
        joinLinkDto.setJoinLinkAddr(joinLink);
        joinLinkDto.setStdUserId(studentId);

        parentJoinMapper.insertParentJoinLink(joinLinkDto);
        smsService.sendSms(phoneNumber, text);

    }

    public void validateJoinLink(String joinLink) {
        ParentJoinLinkDto joinLinkDto = parentJoinMapper.findByJoinLinkAddr(joinLink);
        if (joinLinkDto == null) {
            throw new FinalProjectException(ErrorCode.INVALID_JOIN_LINK);
        }
        if (joinLinkDto.getLinkExprDt().isBefore(java.time.LocalDateTime.now())) {
            throw new FinalProjectException(ErrorCode.EXPIRED_JOIN_LINK);
        }
    }

    public ParentJoinLinkDto validateJoinLinkWithStudentId(String joinLink, String studentId) {
        ParentJoinLinkDto joinLinkDto = parentJoinMapper.findByJoinLinkAddr(joinLink);
        if (joinLinkDto == null || !joinLinkDto.getStdUserId().equals(studentId)) {
            throw new FinalProjectException(ErrorCode.INVALID_JOIN_LINK);
        }
        if (joinLinkDto.getLinkExprDt().isBefore(java.time.LocalDateTime.now())) {
            throw new FinalProjectException(ErrorCode.EXPIRED_JOIN_LINK);
        }
        return joinLinkDto;
    }



    public MemberDto getStudentInfoByJoinLink(String joinLink) {
        ParentJoinLinkDto joinLinkDto = parentJoinMapper.findByJoinLinkAddr(joinLink);
        if (joinLinkDto == null) {
            throw new FinalProjectException(ErrorCode.INVALID_JOIN_LINK);
        }
        String studentId = joinLinkDto.getStdUserId();

        MemberDto studentInfo = memberMapper.findByUserId(studentId)
                .orElseThrow(() -> new FinalProjectException(ErrorCode.STUDENT_NOT_FOUND));

        return studentInfo;

    }

    @Transactional
    public MemberDto joinParent(Authentication authentication, String joinLink, String studentId) {
        // joinLink과 studentId 검증    
        ParentJoinLinkDto joinLinkDto = validateJoinLinkWithStudentId(joinLink, studentId);

        String parentUserId = authentication.getName();
        MemberDto parent = memberMapper.findByUserId(parentUserId)
                .orElseThrow(() -> new FinalProjectException(ErrorCode.USER_NOT_FOUND));


        // 링크 삭제
        parentJoinMapper.deleteParentJoinLink(joinLinkDto.getLinkId());
        // 부모-자식 관계 설정
        StudentDto studentDto = new StudentDto();
        studentDto.setStdUserId(studentId);
        studentDto.setPrntUserId(parent.getUserId());
        studentDto.setPrntTelno(parent.getUserTelno());
        int result = parentJoinMapper.insertParentChildRelation(studentDto);

        if (result <= 0) {
            throw new FinalProjectException(ErrorCode.PARENT_REGISTER_FAILED);
        }


        // 부모의 ROLE_USER -> ROLE_PARENT로 변경
        int resultRoleUpdate = memberMapper.updateUserRole(parent.getUserId(), "ROLE_PARENT");
        if (resultRoleUpdate <= 0) {
            throw new FinalProjectException(ErrorCode.PARENT_REGISTER_FAILED);
        }
        return null;
    }
}
