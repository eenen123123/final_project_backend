package kr.or.ddit.controller.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.or.ddit.finalProject.dto.example.ExampleDto;
import kr.or.ddit.finalProject.dto.file.FileCtxType;
import kr.or.ddit.finalProject.dto.file.FileDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.dto.member.MemberRoleEnum;
import kr.or.ddit.finalProject.mapper.MemberMapper;
import kr.or.ddit.finalProject.mapper.TestMapper;
import kr.or.ddit.finalProject.service.file.FileUploadService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/test")
public class ExampleAdminController {


    @Autowired
    private TestMapper testMapper;
    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public String hello(Model model, Authentication authentication) {
        log.info("ExampleAdminController - hello() called");
        ExampleDto exampleDto = testMapper.getDate();
        model.addAttribute("message", "Hello, the date from DB is: " + exampleDto.getExampleDate());

        String userId = authentication.getName();
        log.info("Authenticated user ID: {}", userId);

        log.info("User details: {}", authentication.getPrincipal());

        log.info("User authorities: {}", authentication.getAuthorities());

        MemberDto member = memberMapper.findByUserId(userId).orElse(null);
        model.addAttribute("user", member);

        return "admin:/hello";
    }

    @PostMapping
    public String postMethodName(@RequestParam MultipartFile file,
            RedirectAttributes redirectAttributes, Authentication authentication) {

        log.info("Received file: {}", file.getOriginalFilename());
        String userId = authentication.getName();
        FileDto response = fileUploadService.uploadFile(file, userId, FileCtxType.MEMBER_ROLE,
                MemberRoleEnum.ROLE_ADMIN.name());
        log.info("File uploaded successfully: {}", response);
        redirectAttributes.addFlashAttribute("fileResponse", response);

        return "redirect:/admin/test";
    }

    @GetMapping("/noti")
    @ResponseBody
    public void sendNotification(@RequestParam String message, Authentication authentication) {
        String userId = authentication.getName();
        String examMessage = """
                asdasd
                asd

                asd
                asd


                        """;
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, examMessage);
    }
}
