package kr.or.ddit.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import kr.or.ddit.finalProject.dto.approval.ApprovalTemplateDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest

public class ApprovalMapperTest {

    @Autowired
    ApprovalMapper approvalMapper;

    @Test
    void getTemplate() {
        String tmplCd = "MEETING";
        ApprovalTemplateDto template = approvalMapper.selectApprovalTemplateById(tmplCd);
        log.info("Retrieved template: {}", template);
    }

    @Test
    void getTemplateList() {
        log.info("Testing getApprovalTemplateList");
        var templates = approvalMapper.selectApprovalTemplateList();
        log.info("Retrieved {} templates", templates.size());
        templates.forEach(template -> log.info("Template: {}", template));

        templates.stream().filter(tmp -> tmp.getTmplCd().equals("MEETING"))
                .forEach(template -> log.info("Template: {}", template));
    }
}
