package kr.or.ddit.finalProject.mapper.board;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.board.FaqDto;

@Mapper
public interface FaqMapper {

    // FAQ 목록 조회 (대분류, 중분류 필터)
    List<FaqDto> findFaqList(@Param("faqCtgCd") String faqCtgCd,
                             @Param("faqSubCtgCd") String faqSubCtgCd);

    // FAQ 단건 조회
    FaqDto findFaqById(@Param("postSn") Long postSn);

    // FAQ 등록
    int insertFaq(FaqDto faqDto);

    // FAQ 수정
    int updateFaq(FaqDto faqDto);

    // FAQ 삭제
    int deleteFaq(@Param("postSn") Long postSn);

    // FAQ 이전글 (같은 카테고리)
    FaqDto findPrevFaq(@Param("postSn") Long postSn, @Param("faqCtgCd") String faqCtgCd);
    
    // FAQ 다음글 (같은 카테고리)
    FaqDto findNextFaq(@Param("postSn") Long postSn, @Param("faqCtgCd") String faqCtgCd);

}
