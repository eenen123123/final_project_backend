package kr.or.ddit.finalProject.service.board.faq;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.board.BoardDto;
import kr.or.ddit.finalProject.dto.board.FaqDto;
import kr.or.ddit.finalProject.mapper.board.FaqMapper;
import kr.or.ddit.finalProject.service.board.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class FaqServiceImpl implements FaqService {

    private final FaqMapper faqMapper;
    private final BoardService boardService;

    @Override
    @Transactional(readOnly = true)
    public List<FaqDto> getFaqList(String faqCtgCd, String faqSubCtgCd) {
        log.info("FAQ 목록 조회 - 대분류: {}, 중분류: {}", faqCtgCd, faqSubCtgCd);
        return faqMapper.findFaqList(faqCtgCd, faqSubCtgCd);
    }

    @Override
    @Transactional(readOnly = true)
    public FaqDto getFaqById(Long postSn) {
        log.info("FAQ 단건 조회 - postSn: {}", postSn);
        return faqMapper.findFaqById(postSn);
    }

    @Override
    @Transactional
    public void createFaq(FaqDto faqDto) {
        // 1. BOARD INSERT → postSn 자동 채번
        BoardDto boardDto = BoardDto.builder().wrtrUserId(faqDto.getWrtrUserId())
                .postSj(faqDto.getPostSj()).postCn(faqDto.getPostCn()).build();
        boardService.createPost(boardDto);

        // 2. 채번된 postSn FAQ에 세팅
        faqDto.setPostSn(boardDto.getPostSn());

        // 3. FAQ INSERT
        faqMapper.insertFaq(faqDto);
    }

    @Override
    @Transactional
    public void updateFaq(FaqDto faqDto) {
        // 1. BOARD 수정
        BoardDto boardDto = BoardDto.builder().postSn(faqDto.getPostSn()).postSj(faqDto.getPostSj())
                .postCn(faqDto.getPostCn()).lastMdfrId(faqDto.getWrtrUserId()).build();
        boardService.updatePost(boardDto);

        // 2. FAQ 수정
        faqMapper.updateFaq(faqDto);
    }

    @Override
    @Transactional
    public void deleteFaq(Long postSn) {
        // 1. FAQ 삭제 (FK 때문에 FAQ 먼저)
        faqMapper.deleteFaq(postSn);

        // 2. BOARD 삭제
        boardService.deletePost(postSn);
    }

}
