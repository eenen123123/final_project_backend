package kr.or.ddit.finalProject.service.board;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.board.BoardDto;
import kr.or.ddit.finalProject.mapper.board.BoardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class BoardServiceImpl implements BoardService {

    private final BoardMapper boardMapper;

    @Override
    @Transactional
    public void createPost(BoardDto boardDto) {
        boardMapper.insertBoard(boardDto);
    }

    @Override
    @Transactional
    public void updatePost(BoardDto boardDto) {
        boardMapper.updateBoard(boardDto);
    }

    @Override
    @Transactional
    public void deletePost(Long postSn) {
        boardMapper.deleteBoard(postSn);
    }

}
