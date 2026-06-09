package kr.or.ddit.finalProject.mapper.textbook;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.textbook.TextbookDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

@Mapper
public interface TextbookMapper {

    // 교재 목록 조회 (페이징 + 필터)
    List<TextbookDto> selectTextbookList(PaginationInfo<TextbookDto> paginationInfo);

    // 교재 총 개수 (페이징용)
    int countTextbookList(PaginationInfo<TextbookDto> paginationInfo);

    // 교재 단건 조회
    TextbookDto selectTextbookBySn(@Param("textbookSn") Long textbookSn);

    // 교재 등록
    int insertTextbook(TextbookDto textbookDto);

    // 교재 수정
    int updateTextbook(TextbookDto textbookDto);

    // 교재 삭제 (논리 삭제)
    int deleteTextbook(@Param("textbookSn") Long textbookSn,
            @Param("lastMdfrId") String lastMdfrId);
}
