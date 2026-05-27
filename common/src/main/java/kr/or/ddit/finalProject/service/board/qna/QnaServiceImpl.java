package kr.or.ddit.finalProject.service.board.qna;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.board.BoardDto;
import kr.or.ddit.finalProject.dto.board.QnaDto;
import kr.or.ddit.finalProject.mapper.board.QnaMapper;
import kr.or.ddit.finalProject.service.board.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class QnaServiceImpl implements QnaService {

    private final QnaMapper qnaMapper;
    private final BoardService boardService;

    @Override
    @Transactional(readOnly = true)
    public List<QnaDto> getQnaList(String qnaCtgCd, String answStatCd) {
        return qnaMapper.findQnaList(qnaCtgCd, answStatCd);
    }

    @Override
    @Transactional(readOnly = true)
    public QnaDto getQnaById(Long postSn) {
        return qnaMapper.findQnaById(postSn);
    }

    @Override
    @Transactional
    public void createQna(QnaDto qnaDto) {
        // 1. BOARD INSERT → postSn 자동 채번
        BoardDto boardDto = BoardDto.builder().wrtrUserId(qnaDto.getWrtrUserId())
                .postSj(qnaDto.getPostSj()).postCn(qnaDto.getPostCn()).build();
        boardService.createPost(boardDto);

        // 2. 채번된 postSn QnA에 세팅
        qnaDto.setPostSn(boardDto.getPostSn());

        // 3. QnA INSERT
        qnaMapper.insertQna(qnaDto);
    }

    @Override
    @Transactional
    public void updateQna(QnaDto qnaDto) {
        // 1. BOARD 수정
        BoardDto boardDto = BoardDto.builder().postSn(qnaDto.getPostSn()).postSj(qnaDto.getPostSj())
                .postCn(qnaDto.getPostCn()).lastMdfrId(qnaDto.getWrtrUserId()).build();
        boardService.updatePost(boardDto);

        // 2. QnA 수정
        qnaMapper.updateQna(qnaDto);
    }

    @Override
    @Transactional
    public void answerQna(QnaDto qnaDto) {
        // 답변 등록 + 답변상태 완료로 변경
        qnaMapper.updateQnaAnswer(qnaDto);
    }

    @Override
    @Transactional
    public void deleteQna(Long postSn) {
        // 1. QnA 삭제 (FK 때문에 먼저)
        qnaMapper.deleteQna(postSn);

        // 2. BOARD 삭제
        boardService.deletePost(postSn);
    }

}
