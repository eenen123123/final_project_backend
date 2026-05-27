package kr.or.ddit.finalProject.mapper.board;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.board.NoticeDto;

@Mapper
public interface NoticeMapper {

    // 공지사항 목록 조회
    List<NoticeDto> findNoticeList(@Param("noticeTypeCd") String noticeTypeCd);

    // 공지사항 단건 조회
    NoticeDto findNoticeById(@Param("postSn") Long postSn);

    // 공지사항 INSERT
    int insertNotice(NoticeDto noticeDto);

    // 공지사항 UPDATE
    int updateNotice(NoticeDto noticeDto);

    // 공지사항 DELETE
    int deleteNotice(@Param("postSn") Long postSn);

}
