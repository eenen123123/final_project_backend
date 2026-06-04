package kr.or.ddit.finalProject.mapper.board;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.board.QnaDto;
import kr.or.ddit.finalProject.dto.board.req.QnaSearchCondition;
import kr.or.ddit.finalProject.paging.PaginationInfo;

@Mapper
public interface QnaMapper {

    // QnA 목록 조회
    List<QnaDto> findQnaList(@Param("qnaCtgCd") String qnaCtgCd,
            @Param("answStatCd") String answStatCd);

    // QnA 페이징 목록 조회
    List<QnaDto> findQnaListPaged(PaginationInfo<QnaSearchCondition> paginationInfo);

    // QnA 전체 건수 조회
    int countQnaList(PaginationInfo<QnaSearchCondition> paginationInfo);

    // QnA 단건 조회
    QnaDto findQnaById(@Param("postSn") Long postSn);

    // QnA INSERT
    int insertQna(QnaDto qnaDto);

    // QnA UPDATE
    int updateQna(QnaDto qnaDto);

    // QnA 답변 등록
    int updateQnaAnswer(QnaDto qnaDto);

    // QnA DELETE
    int deleteQna(@Param("postSn") Long postSn);

}
