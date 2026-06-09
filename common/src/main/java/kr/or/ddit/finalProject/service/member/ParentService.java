package kr.or.ddit.finalProject.service.member;

import java.util.List;
import org.springframework.stereotype.Service;
import kr.or.ddit.finalProject.dto.user.ParentJoinLinkDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
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

    public void sendParentJoinLink(String phoneNumber, String baseUrl, String studentId) {

        // 학생 id 검증 로직
        staffMapper.selectStudentList().stream()
                .filter(student -> student.getUserId().equals(studentId)).findAny()
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

        smsService.sendSms(phoneNumber, text);


        ParentJoinLinkDto joinLinkDto = new ParentJoinLinkDto();
        joinLinkDto.setJoinLinkAddr(joinLink);
        joinLinkDto.setStdUserId(studentId);

        parentJoinMapper.insertParentJoinLink(joinLinkDto);

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

    public String getJoinTokenFromJoinLink(String joinLink) {
        ParentJoinLinkDto joinLinkDto = parentJoinMapper.findByJoinLinkAddr(joinLink);
        if (joinLinkDto == null) {
            throw new FinalProjectException(ErrorCode.INVALID_JOIN_LINK);
        }
        return joinLinkDto.getJoinLinkAddr()
                .substring(joinLinkDto.getJoinLinkAddr().indexOf("token=") + 6);

    }
}
