package kr.or.ddit.finalProject.service.post;

import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.post.PostMasterDto;
import kr.or.ddit.finalProject.dto.post.PostReceiverDto;
import kr.or.ddit.finalProject.dto.post.PostSearchCondition;
import kr.or.ddit.finalProject.dto.post.PostSenderDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.post.PostMapper;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;



    @Override
    @Transactional
    public void createPost(PostMasterDto postMasterDto, Authentication authentication,
            String receiverUserId) {
        String userId = authentication.getName();
        postMasterDto.setNtceSndDt(LocalDateTime.now());
        int result = postMapper.insertPostMaster(postMasterDto);
        if (result > 0) {
            Long postSn = postMasterDto.getNtceSn();
            // 발신자 정보 저장
            PostSenderDto senderDto = new PostSenderDto();
            senderDto.setSndrUserId(userId);
            senderDto.setNtceSn(postSn);
            postMapper.insertPostSender(senderDto);
            // 수신자 정보 저장
            PostReceiverDto receiverDto = new PostReceiverDto();
            receiverDto.setRcvrUserId(receiverUserId);
            receiverDto.setNtceSn(postSn);
            postMapper.insertPostReceiver(receiverDto);
        } else {
            throw new FinalProjectException(ErrorCode.POST_MESSAGE_CREATE_FAIL);
        }
    }

    @Override
    public void deletePost(Long postSn, Authentication authentication) {
        // TODO Auto-generated method stub

    }

    @Override
    public PostMasterDto getPostById(Long postSn, Authentication authentication) {
        String userId = authentication.getName();
        return postMapper.selectPostById(postSn, userId);
    }

    @Override
    public PageResponse<PostMasterDto> getPostList(
            PaginationInfo<PostSearchCondition> paginationInfo, Authentication authentication) {
        String userId = authentication.getName();
        List<PostMasterDto> items = postMapper.selectPostList(paginationInfo, userId);
        int totalCount = postMapper.selectPostListCount(paginationInfo, userId);
        return new PageResponse<>(items, totalCount);
    }



}
