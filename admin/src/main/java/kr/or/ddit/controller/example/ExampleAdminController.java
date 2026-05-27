package kr.or.ddit.controller.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.or.ddit.finalProject.dto.example.ExampleDto;
import kr.or.ddit.finalProject.dto.file.FileDto;
import kr.or.ddit.finalProject.dto.file.StoredFileResponse;
import kr.or.ddit.finalProject.dto.user.UserDto;
import kr.or.ddit.finalProject.mapper.TestMapper;
import kr.or.ddit.finalProject.mapper.UserMapper;
import kr.or.ddit.finalProject.service.file.FileUploadService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/test")
public class ExampleAdminController {

    @Autowired
    private TestMapper testMapper;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FileUploadService fileUploadService;

    @GetMapping
    public String hello(Model model, Authentication authentication) {
        log.info("ExampleAdminController - hello() called");
        ExampleDto exampleDto = testMapper.getDate();
        model.addAttribute("message", "Hello, the date from DB is: " + exampleDto.getExampleDate());

        String userId = authentication.getName();
        log.info("Authenticated user ID: {}", userId);

        log.info("User details: {}", authentication.getPrincipal());

        log.info("User authorities: {}", authentication.getAuthorities());

        UserDto member = userMapper.findByUserId(userId).orElse(null);
        model.addAttribute("user", member);

        return "admin:/hello";
    }

    @PostMapping
    public String postMethodName(@RequestParam MultipartFile file,
            RedirectAttributes redirectAttributes, Authentication authentication) {

        log.info("Received file: {}", file.getOriginalFilename());
        String userId = authentication.getName();
        FileDto response = fileUploadService.uploadFile(file, userId);
        log.info("File uploaded successfully: {}", response);
        redirectAttributes.addFlashAttribute("fileResponse", response);

        return "redirect:/admin/test";
    }

}
