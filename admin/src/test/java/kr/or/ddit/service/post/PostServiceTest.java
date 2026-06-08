package kr.or.ddit.service.post;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.member.MemberRoleEnum;
import kr.or.ddit.finalProject.dto.post.PostMasterDto;
import kr.or.ddit.finalProject.dto.post.PostSearchCondition;
import kr.or.ddit.finalProject.dto.post.PostTypeEnum;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.finalProject.service.member.MemberService;
import kr.or.ddit.finalProject.service.post.PostService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public class PostServiceTest {

    @Autowired
    PostService postService;
    @Autowired
    MemberService memberService;
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser01");

        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext(); // 테스트 간 오염 방지
    }

    @Test
    @Transactional
    void insertPostTest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String receiverUserId = "testuser02";
        PostMasterDto postMasterDto = PostMasterDto.builder().ntceSj("테스트 쪽지 제목")
                .ntceCn("테스트 쪽지 내용").ntceTypeCd(PostTypeEnum.PERSONAL).build();


        postService.createPost(postMasterDto, authentication, receiverUserId);

        Long createdSn = postMasterDto.getNtceSn();
        // PostMasterDto result = postService.getPostById(createdSn, authentication);
        // log.info("조회된 쪽지: {}", result);
    }

    @Test
    void selectPostListTest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PaginationInfo<PostSearchCondition> paginationInfo = new PaginationInfo<>(10, 5, 1);
        paginationInfo.setDetailCondition(
                new PostSearchCondition(null, null, "all", "sent", PostTypeEnum.PERSONAL));

        PageResponse<PostMasterDto> postList =
                postService.getPostList(paginationInfo, authentication);

        log.info("쪽지 리스트: {}", postList.getItems());

    }
}
