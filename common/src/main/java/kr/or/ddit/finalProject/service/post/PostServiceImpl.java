package kr.or.ddit.finalProject.service.post;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            String... receiverUserIds) {
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
            List<PostReceiverDto> receivers = Arrays.stream(receiverUserIds)
                    .map(id -> { PostReceiverDto r = new PostReceiverDto(); r.setNtceSn(postSn); r.setRcvrUserId(id); return r; })
                    .collect(Collectors.toList());
            postMapper.insertPostReceivers(receivers);
        } else {
            throw new FinalProjectException(ErrorCode.POST_MESSAGE_CREATE_FAIL);
        }
    }

    private void applyDelStatus(Long postSn, String userId, String delYn) {
        int updated = postMapper.updateSenderDelStatus(postSn, userId, delYn)
                    + postMapper.updateReceiverDelStatus(postSn, userId, delYn);
        if (updated == 0) {
            throw new FinalProjectException(ErrorCode.POST_NOT_FOUND);
        }
    }

    @Override
    public void deletePost(Long postSn, Authentication authentication) {
        applyDelStatus(postSn, authentication.getName(), "S");
    }

    @Override
    public void restorePost(Long postSn, Authentication authentication) {
        applyDelStatus(postSn, authentication.getName(), "N");
    }

    @Override
    public void permanentDeletePost(Long postSn, Authentication authentication) {
        applyDelStatus(postSn, authentication.getName(), "Y");
    }

    private void applyArchiveYn(Long postSn, String userId, String archiveYn) {
        int updated = postMapper.updateSenderArchiveYn(postSn, userId, archiveYn)
                    + postMapper.updateReceiverArchiveYn(postSn, userId, archiveYn);
        if (updated == 0) {
            throw new FinalProjectException(ErrorCode.POST_NOT_FOUND);
        }
    }

    @Override
    public void archivePost(Long postSn, Authentication authentication) {
        applyArchiveYn(postSn, authentication.getName(), "Y");
    }

    @Override
    public void unarchivePost(Long postSn, Authentication authentication) {
        applyArchiveYn(postSn, authentication.getName(), "N");
    }

    @Override
    public PostMasterDto getPostById(Long postSn, String rcvrUserId, Authentication authentication) {
        String currentUserId = authentication.getName();
        String queryUserId = (rcvrUserId != null && !rcvrUserId.isBlank()) ? rcvrUserId : currentUserId;

        PostMasterDto post = postMapper.selectPostById(postSn, queryUserId);
        if (post == null) {
            throw new FinalProjectException(ErrorCode.POST_NOT_FOUND);
        }

        boolean isSender = currentUserId.equals(post.getSndrUserId());
        boolean isReceiver = currentUserId.equals(post.getRcvrUserId());
        if (!isSender && !isReceiver) {
            throw new FinalProjectException(ErrorCode.POST_NOT_FOUND);
        }

        return post;
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
