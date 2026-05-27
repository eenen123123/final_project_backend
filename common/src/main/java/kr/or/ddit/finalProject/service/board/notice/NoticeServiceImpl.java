package kr.or.ddit.finalProject.service.board.notice;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.board.BoardDto;
import kr.or.ddit.finalProject.dto.board.NoticeDto;
import kr.or.ddit.finalProject.mapper.board.NoticeMapper;
import kr.or.ddit.finalProject.service.board.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;
    private final BoardService boardService;

    @Override
    @Transactional(readOnly = true)
    public List<NoticeDto> getNoticeList(String noticeTypeCd) {
        return noticeMapper.findNoticeList(noticeTypeCd);
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeDto getNoticeById(Long postSn) {
        return noticeMapper.findNoticeById(postSn);
    }

    @Override
    @Transactional
    public void createNotice(NoticeDto noticeDto) {
        // 1. BOARD INSERT → postSn 자동 채번
        BoardDto boardDto = BoardDto.builder().wrtrUserId(noticeDto.getWrtrUserId())
                .postSj(noticeDto.getPostSj()).postCn(noticeDto.getPostCn())
                .topFixOrd(noticeDto.getTopFixOrd()).popupExpsYn(noticeDto.getPopupExpsYn())
                .build();
        boardService.createPost(boardDto);

        // 2. 채번된 postSn NOTICE에 세팅
        noticeDto.setPostSn(boardDto.getPostSn());

        // 3. NOTICE INSERT
        noticeMapper.insertNotice(noticeDto);
    }

    @Override
    @Transactional
    public void updateNotice(NoticeDto noticeDto) {
        // 1. BOARD 수정
        BoardDto boardDto = BoardDto.builder().postSn(noticeDto.getPostSn())
                .postSj(noticeDto.getPostSj()).postCn(noticeDto.getPostCn())
                .topFixOrd(noticeDto.getTopFixOrd()).popupExpsYn(noticeDto.getPopupExpsYn())
                .lastMdfrId(noticeDto.getWrtrUserId()).build();
        boardService.updatePost(boardDto);

        // 2. NOTICE 수정
        noticeMapper.updateNotice(noticeDto);
    }

    @Override
    @Transactional
    public void deleteNotice(Long postSn) {
        // 1. NOTICE 삭제 (FK 때문에 먼저)
        noticeMapper.deleteNotice(postSn);

        // 2. BOARD 삭제
        boardService.deletePost(postSn);
    }

}
