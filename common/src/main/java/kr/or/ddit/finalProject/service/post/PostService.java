package kr.or.ddit.finalProject.service.post;

import org.springframework.security.core.Authentication;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.post.PostMasterDto;
import kr.or.ddit.finalProject.dto.post.PostSearchCondition;
import kr.or.ddit.finalProject.paging.PaginationInfo;

public interface PostService {

    /**
     * 쪽지 목록 조회
     * @param paginationInfo
     * @return
     */
    PageResponse<PostMasterDto> getPostList(PaginationInfo<PostSearchCondition> paginationInfo,
            Authentication authentication);

    // 쪽지 상세 조회
    PostMasterDto getPostById(Long postSn, Authentication authentication);

    // 쪽지 작성
    void createPost(PostMasterDto postMasterDto, Authentication authentication,
            String receiverUserId);

    // 쪽지 삭제
    void deletePost(Long postSn, Authentication authentication);


}
