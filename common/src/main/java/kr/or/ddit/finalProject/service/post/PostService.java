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
    PostMasterDto getPostById(Long postSn, String rcvrUserId, Authentication authentication);

    // 쪽지 작성
    void createPost(PostMasterDto postMasterDto, Authentication authentication,
            String... receiverUserIds);

    // 쪽지 삭제 (휴지통으로)
    void deletePost(Long postSn, Authentication authentication);

    // 쪽지 복원 (휴지통 → 원래 함)
    void restorePost(Long postSn, Authentication authentication);

    // 완전 삭제 (모든 뷰에서 숨김)
    void permanentDeletePost(Long postSn, Authentication authentication);

    // 보관함으로 이동
    void archivePost(Long postSn, Authentication authentication);

    // 보관 해제 (원래 함으로 복귀)
    void unarchivePost(Long postSn, Authentication authentication);


}
