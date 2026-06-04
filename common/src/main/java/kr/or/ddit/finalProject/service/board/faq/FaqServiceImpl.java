package kr.or.ddit.finalProject.service.board.faq;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.board.FaqDto;
import kr.or.ddit.finalProject.dto.board.req.FaqSearchCondition;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.mapper.board.BoardMapper;
import kr.or.ddit.finalProject.mapper.board.FaqMapper;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class FaqServiceImpl implements FaqService {

    private final FaqMapper faqMapper;
    private final BoardMapper boardMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FaqDto> getList(PaginationInfo<FaqSearchCondition> paginationInfo) {
        List<FaqDto> items = faqMapper.findFaqListPaged(paginationInfo);
        int totalCount = faqMapper.countFaqList(paginationInfo);
        return new PageResponse<>(items, totalCount);
    }

    @Override
    @Transactional(readOnly = true)
    public FaqDto getById(Long postSn, Authentication authentication) {
        return faqMapper.findFaqById(postSn);
    }

    @Override
    @Transactional
    public void create(FaqDto dto, Authentication authentication) {
        if (authentication != null) {
            dto.setWrtrUserId(authentication.getName());
        }
        boardMapper.insertBoard(dto);
        faqMapper.insertFaq(dto);
    }

    @Override
    @Transactional
    public void update(FaqDto dto) {
        boardMapper.updateBoard(dto);
        faqMapper.updateFaq(dto);
    }

    @Override
    @Transactional
    public void delete(Long postSn) {
        faqMapper.deleteFaq(postSn);
        boardMapper.deleteBoard(postSn);
    }

    @Override
    @Transactional(readOnly = true)
    public FaqDto getPrevFaq(Long postSn, String faqCtgCd) {
        return faqMapper.findPrevFaq(postSn, faqCtgCd);
    }

    @Override
    @Transactional(readOnly = true)
    public FaqDto getNextFaq(Long postSn, String faqCtgCd) {
        return faqMapper.findNextFaq(postSn, faqCtgCd);
    }
}
