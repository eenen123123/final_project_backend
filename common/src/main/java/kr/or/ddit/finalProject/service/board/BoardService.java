package kr.or.ddit.finalProject.service.board;

import org.springframework.security.core.Authentication;

import kr.or.ddit.finalProject.dto.board.BoardDto;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.paging.PaginationInfo;

public interface BoardService<T extends BoardDto, C> {

    PageResponse<T> getList(PaginationInfo<C> paginationInfo);

    T getById(Long postSn, Authentication authentication);

    void create(T dto, Authentication authentication);

    void update(T dto);

    void delete(Long postSn);
}
