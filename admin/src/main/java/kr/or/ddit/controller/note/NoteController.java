package kr.or.ddit.controller.note;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import kr.or.ddit.finalProject.dto.post.PostMasterDto;
import kr.or.ddit.finalProject.dto.post.PostSearchCondition;
import kr.or.ddit.finalProject.dto.post.PostTypeEnum;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.post.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequestMapping("/admin/note")
@RequiredArgsConstructor
public class NoteController {

    private final PostService postService;

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
        log.info("postList: {}", postList);
        model.addAttribute("postList", postList);
        model.addAttribute("box", box);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);
        model.addAttribute("readType", readType);
        return "admin:/note/noteList";
    }
}
