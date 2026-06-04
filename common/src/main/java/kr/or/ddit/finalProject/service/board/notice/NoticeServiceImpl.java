package kr.or.ddit.finalProject.service.board.notice;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.board.NoticeDto;
import kr.or.ddit.finalProject.dto.board.req.NoticeSearchCondition;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.mapper.board.BoardMapper;
import kr.or.ddit.finalProject.mapper.board.NoticeMapper;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;
    private final BoardMapper boardMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NoticeDto> getList(PaginationInfo<NoticeSearchCondition> paginationInfo) {
        List<NoticeDto> items = noticeMapper.findNoticeListPaged(paginationInfo);
        int totalCount = noticeMapper.countNoticeList(paginationInfo);
        return new PageResponse<>(items, totalCount);
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeDto getById(Long postSn, Authentication authentication) {
        return noticeMapper.findNoticeById(postSn);
    }

    @Override
    @Transactional
    public void create(NoticeDto dto, Authentication authentication) {
        if (authentication != null) {
            dto.setWrtrUserId(authentication.getName());
        }
        boardMapper.insertBoard(dto);
        noticeMapper.insertNotice(dto);
    }

    @Override
    @Transactional
    public void update(NoticeDto dto) {
        boardMapper.updateBoard(dto);
        noticeMapper.updateNotice(dto);
    }

    @Override
    @Transactional
    public void delete(Long postSn) {
        noticeMapper.deleteNotice(postSn);
        boardMapper.deleteBoard(postSn);
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeDto getPrevNotice(Long postSn) {
        return noticeMapper.findPrevNotice(postSn);
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeDto getNextNotice(Long postSn) {
        return noticeMapper.findNextNotice(postSn);
    }
}
