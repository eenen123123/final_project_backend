package kr.or.ddit.finalProject.mapper.board;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.board.FaqDto;
import kr.or.ddit.finalProject.dto.board.req.FaqSearchCondition;
import kr.or.ddit.finalProject.paging.PaginationInfo;

@Mapper
public interface FaqMapper {

    List<FaqDto> findFaqList(@Param("faqCtgCd") String faqCtgCd, @Param("faqSubCtgCd") String faqSubCtgCd);

    List<FaqDto> findFaqListPaged(PaginationInfo<FaqSearchCondition> paginationInfo);

    int countFaqList(PaginationInfo<FaqSearchCondition> paginationInfo);

    FaqDto findFaqById(@Param("postSn") Long postSn);

    int insertFaq(FaqDto faqDto);

    int updateFaq(FaqDto faqDto);

    int deleteFaq(@Param("postSn") Long postSn);

    FaqDto findPrevFaq(@Param("postSn") Long postSn, @Param("faqCtgCd") String faqCtgCd);

    FaqDto findNextFaq(@Param("postSn") Long postSn, @Param("faqCtgCd") String faqCtgCd);
}
