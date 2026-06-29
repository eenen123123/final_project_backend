package kr.or.ddit.finalProject.service.board.qna;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.or.ddit.finalProject.aop.ActivityTargetIdHolder;
import kr.or.ddit.finalProject.dto.board.QnaDto;
import kr.or.ddit.finalProject.dto.board.req.QnaSearchCondition;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.member.MemberRoleEnum;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.board.BoardMapper;
import kr.or.ddit.finalProject.mapper.board.QnaMapper;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class QnaServiceImpl implements QnaService {

    private final QnaMapper qnaMapper;
    private final BoardMapper boardMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QnaDto> getList(PaginationInfo<QnaSearchCondition> paginationInfo) {
        List<QnaDto> items = qnaMapper.findQnaListPaged(paginationInfo);
        int totalCount = qnaMapper.countQnaList(paginationInfo);
        return new PageResponse<>(items, totalCount);
    }

    @Override
    @Transactional(readOnly = true)
    public QnaDto getById(Long postSn, Authentication authentication) {
        String userId = authentication.getName();
        QnaDto qna = qnaMapper.findQnaById(postSn);
        if (qna == null) {
            throw new FinalProjectException(ErrorCode.POST_NOT_FOUND);
        }
        if ("Y".equals(qna.getSecrYn()) && !qna.getWrtrUserId().equals(userId)) {
            if (authentication.getAuthorities().stream().noneMatch(
                    auth -> auth.getAuthority().equals(MemberRoleEnum.ROLE_ADMIN.name()))) {
                throw new FinalProjectException(ErrorCode.QNA_ACCESS_DENIED);
            }
        }
        return qna;
    }

    @Override
    @Transactional
    public void create(QnaDto dto, Authentication authentication) {
        dto.setWrtrUserId(authentication.getName());
        dto.setBoardTypeCd("03");
        dto.setPopupExpsYn("N");
        boardMapper.insertBoard(dto);
        qnaMapper.insertQna(dto);

        // TargetID
        ActivityTargetIdHolder.set(String.valueOf(dto.getPostSn()));
    }

    @Override
    @Transactional
    public void update(QnaDto dto) {
        boardMapper.updateBoard(dto);
        qnaMapper.updateQna(dto);
    }

    @Override
    @Transactional
    public void update(QnaDto dto, Authentication authentication) {
        QnaDto existing = qnaMapper.findQnaById(dto.getPostSn());
        if (existing == null) throw new FinalProjectException(ErrorCode.POST_NOT_FOUND);
        if (!existing.getWrtrUserId().equals(authentication.getName()))
            throw new FinalProjectException(ErrorCode.QNA_ACCESS_DENIED);
        boardMapper.updateBoard(dto);
        qnaMapper.updateQna(dto);
    }

    @Override
    @Transactional
    public void answerQna(QnaDto qnaDto) {
        qnaMapper.updateQnaAnswer(qnaDto);
    }

    @Override
    @Transactional
    public void delete(Long postSn) {
        qnaMapper.deleteQna(postSn);
        boardMapper.deleteBoard(postSn);
    }

    @Override
    @Transactional
    public void delete(Long postSn, Authentication authentication) {
        QnaDto existing = qnaMapper.findQnaById(postSn);
        if (existing == null) throw new FinalProjectException(ErrorCode.POST_NOT_FOUND);
        if (!existing.getWrtrUserId().equals(authentication.getName()))
            throw new FinalProjectException(ErrorCode.QNA_ACCESS_DENIED);
        qnaMapper.deleteQna(postSn);
        boardMapper.deleteBoard(postSn);
    }
}
