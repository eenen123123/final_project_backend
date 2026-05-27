package kr.or.ddit.finalProject.service.board.qna;

import java.util.List;

import kr.or.ddit.finalProject.dto.board.QnaDto;

public interface QnaService {

    /**
     * QnA 목록 조회
     *
     * @param qnaCtgCd QnA 카테고리 코드 (CL_CODE: 105), null이면 전체 조회
     * @param answStatCd 답변 상태 코드 (CL_CODE: 104), null이면 전체 조회
     * @return QnA 목록
     */
    List<QnaDto> getQnaList(String qnaCtgCd, String answStatCd);

    /**
     * QnA 단건 조회
     *
     * @param postSn QnA PK
     * @return QnA 상세 정보
     */
    QnaDto getQnaById(Long postSn);

    /**
     * QnA 등록
     *
     * @param qnaDto 등록할 QnA 정보
     */
    void createQna(QnaDto qnaDto);

    /**
     * QnA 수정
     *
     * @param qnaDto 수정할 QnA 정보
     */
    void updateQna(QnaDto qnaDto);

    /**
     * QnA 답변 등록
     *
     * @param qnaDto 답변 정보
     */
    void answerQna(QnaDto qnaDto);

    /**
     * QnA 삭제
     *
     * @param postSn 삭제할 QnA PK
     */
    void deleteQna(Long postSn);

}
