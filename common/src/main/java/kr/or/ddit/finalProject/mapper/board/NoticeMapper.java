package kr.or.ddit.finalProject.mapper.board;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.board.NoticeDto;
import kr.or.ddit.finalProject.dto.board.req.NoticeSearchCondition;
import kr.or.ddit.finalProject.paging.PaginationInfo;

@Mapper
public interface NoticeMapper {

    List<NoticeDto> findNoticeList(@Param("noticeTypeCd") String noticeTypeCd);

    List<NoticeDto> findNoticeListPaged(PaginationInfo<NoticeSearchCondition> paginationInfo);

    int countNoticeList(PaginationInfo<NoticeSearchCondition> paginationInfo);

    NoticeDto findNoticeById(@Param("postSn") Long postSn);

    int insertNotice(NoticeDto noticeDto);

    int updateNotice(NoticeDto noticeDto);

    int deleteNotice(@Param("postSn") Long postSn);

    NoticeDto findPrevNotice(@Param("postSn") Long postSn);

    NoticeDto findNextNotice(@Param("postSn") Long postSn);
}
