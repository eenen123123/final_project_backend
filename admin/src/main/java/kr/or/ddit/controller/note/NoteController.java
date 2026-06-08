package kr.or.ddit.controller.note;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import kr.or.ddit.finalProject.dto.member.AdminMemberDto;
import kr.or.ddit.finalProject.dto.post.PostMasterDto;
import kr.or.ddit.finalProject.dto.post.PostSearchCondition;
import kr.or.ddit.finalProject.dto.post.PostTypeEnum;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.member.MemberService;
import kr.or.ddit.finalProject.service.post.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@Controller
@RequestMapping("/admin/note")
@RequiredArgsConstructor
public class NoteController {

    private final PostService postService;
    private final MemberService memberService;

    @GetMapping
    public String getNoteListPage(@RequestParam(name = "page", defaultValue = "1") int page, // 페이지 번호, 기본값은 1
            @RequestParam(name = "box", defaultValue = "received") String box, // 쪽지함 종류 (received, sent), 기본값은 received
            @RequestParam(name = "searchType", required = false) String searchType, // 검색 유형 (sender, receiver, content), 선택적
            @RequestParam(name = "keyword", required = false) String keyword, // 검색 키워드, 선택적
            @RequestParam(name = "sort", defaultValue = "date_desc") String sort, // 정렬 기준, 기본값은 date_desc
            @RequestParam(name = "readType", defaultValue = "all") String readType // 읽음 상태, 기본값은 all (read, unread, all)
            , @RequestParam(name = "postType", defaultValue = "PERSONAL") String postType,
            Model model, Authentication authentication

    ) {
        PaginationInfo<PostSearchCondition> paginationInfo = new PaginationInfo<>(10, 5, page);
        PostTypeEnum postTypeEnum;
        try {
            postTypeEnum = PostTypeEnum.valueOf(postType.toUpperCase());
        } catch (IllegalArgumentException e) {
            postTypeEnum = PostTypeEnum.PERSONAL; // 기본값으로 설정
        }
        paginationInfo.setDetailCondition(
                new PostSearchCondition(keyword, searchType, readType, box, postTypeEnum));
        List<PostMasterDto> postList =
                postService.getPostList(paginationInfo, authentication).getItems();
        model.addAttribute("postList", postList);
        model.addAttribute("box", box);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);
        model.addAttribute("readType", readType);
        model.addAttribute("postType", postTypeEnum.toString());
        return "admin:/note/noteList";
    }

    @GetMapping("/write")
    public String getWriteNotePage(@RequestParam(required = false) String replyTo,
            @RequestParam(required = false) String subject, Model model,
            Authentication authentication) {
        String userId = authentication.getName();
        Map<String, List<AdminMemberDto>> groupedAdminUsers =
                memberService.getGroupedAdminUsers(userId);
        model.addAttribute("groupedAdminUsers", groupedAdminUsers);
        model.addAttribute("replyTo", replyTo != null ? replyTo : "");
        model.addAttribute("replySubject",
                subject != null && !subject.isBlank() ? "Re: " + subject : "");
        return "/note/noteForm";
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendNote(@RequestParam String title, @RequestParam String content,
            @RequestParam(name = "receiverUserIds", required = false) String[] receiverUserIds,
            Authentication authentication) {
        PostMasterDto postMasterDto = PostMasterDto.builder().ntceSj(title).ntceCn(content).build();
        postMasterDto.setNtceTypeCd(PostTypeEnum.PERSONAL);
        postService.createPost(postMasterDto, authentication, receiverUserIds);
        return ResponseEntity.status(302).header("Location", "/admin/note?box=sent").build();
    }

    @GetMapping("/read")
    public String readNote(@RequestParam("postSn") String postSn,
            @RequestParam(value = "rcvrUserId", required = false) String rcvrUserId, Model model,
            Authentication authentication) {

        PostMasterDto postMasterDto =
                postService.getPostById(Long.valueOf(postSn), rcvrUserId, authentication);
        model.addAttribute("post", postMasterDto);
        return "/note/noteRead";
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteNote(@RequestParam Long postSn,
            Authentication authentication) {
        postService.deletePost(postSn, authentication);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restore")
    public ResponseEntity<Void> restoreNote(@RequestParam Long postSn,
            Authentication authentication) {
        postService.restorePost(postSn, authentication);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete/permanent")
    public ResponseEntity<Void> permanentDeleteNote(@RequestParam Long postSn,
            Authentication authentication) {
        postService.permanentDeletePost(postSn, authentication);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/archive")
    public ResponseEntity<Void> archiveNote(@RequestParam Long postSn,
            Authentication authentication) {
        postService.archivePost(postSn, authentication);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/archive/cancel")
    public ResponseEntity<Void> unarchiveNote(@RequestParam Long postSn,
            Authentication authentication) {
        postService.unarchivePost(postSn, authentication);
        return ResponseEntity.ok().build();
    }

}
