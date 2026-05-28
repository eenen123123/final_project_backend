package kr.or.ddit.finalProject.service.board.faq;

import java.util.List;

import kr.or.ddit.finalProject.dto.board.FaqDto;

public interface FaqService {

    /**
     * FAQ 목록 조회
     *
     * @param faqCtgCd 대분류 코드 (CL_CODE: 101), null 이면 전체 조회
     * @param faqSubCtgCd 중분류 코드 (CL_CODE: 102) null 이면 전체 조회
     * @return FAQ 목록
     */
    List<FaqDto> getFaqList(String faqCtgCd, String faqSubCtgCd);

    /**
     * FAQ 단건 조회
     *
     * @param postSn FAQ 게시글 PK
     * @return FAQ 상세 정보
     */
    FaqDto getFaqById(Long postSn);

    /**
     * FAQ 등록
     *
     * @param faqDto 등록할 FAQ 정보
     */
    void createFaq(FaqDto faqDto);

    /**
     * FAQ 수정
     *
     * @param faqDto 수정할 FAQ 정보
     */
    void updateFaq(FaqDto faqDto);

    /**
     * FAQ 삭제
     *
     * @param postSn 삭제할 FAQ PK
     */
    void deleteFaq(Long postSn);

    /**
     * FAQ 이전글 조회 (같은 카테고리)
     *
     * @param postSn 현재 FAQ PK
     * @param faqCtgCd 대분류 코드
     * @return 이전 FAQ
     */
    FaqDto getPrevFaq(Long postSn, String faqCtgCd);

    /**
     * FAQ 다음글 조회 (같은 카테고리)
     *
     * @param postSn 현재 FAQ PK
     * @param faqCtgCd 대분류 코드
     * @return 다음 FAQ
     */
    FaqDto getNextFaq(Long postSn, String faqCtgCd);

}
