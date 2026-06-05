package kr.or.ddit.finalProject.mapper.post;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import kr.or.ddit.finalProject.dto.post.PostMasterDto;
import kr.or.ddit.finalProject.dto.post.PostReceiverDto;
import kr.or.ddit.finalProject.dto.post.PostSearchCondition;
import kr.or.ddit.finalProject.dto.post.PostSenderDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

@Mapper
public interface PostMapper {

    List<PostMasterDto> selectPostList(@Param("paginationInfo") PaginationInfo<PostSearchCondition> paginationInfo,
            @Param("userId") String userId);

    int selectPostListCount(@Param("paginationInfo") PaginationInfo<PostSearchCondition> paginationInfo,
            @Param("userId") String userId);

    PostMasterDto selectPostById(@Param("postSn") Long postSn, @Param("userId") String userId);

    int insertPostMaster(PostMasterDto postMasterDto);

    int insertPostSender(PostSenderDto postSenderDto);

    int insertPostReceiver(PostReceiverDto postReceiverDto);

}
