package kr.or.ddit.finalProject.paging;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * 페이징 처리에 필요한 정보를 담는 클래스
 */

@Getter
public class PaginationInfo<T> implements Serializable {
    private int screenSize; // 한 페이지에 보여줄 데이터 수
    private int blockSize; // 한 번에 보여줄 페이지 번호 수
    private int page; // 현재 페이지 번호
    private int totalCount; // 전체 데이터 수

    private String orderBy; // 정렬 기준 컬럼명 (ex: mem_id, mem_name 등.. mapper에서 if 문으로 사용)
    private String orderDirection; // 정렬 방향 (ASC(오름차순), DESC(내림차순))

    @Setter
    private T detailCondition; // 검색 할 객체를 담을 제네릭 필드 ex) MemberVO ...

    /**
     * PaginationInfo 객체를 생성하는 생성자
     * 
     * 
     * @param screenSize 한 페이지에 보여줄 데이터 수
     * @param blockSize  한 번에 보여줄 페이지 번호 수
     * @param page       현재 페이지 번호
     */
    public PaginationInfo(int screenSize, int blockSize, int page) {
        this.screenSize = screenSize;
        this.blockSize = blockSize;
        this.page = page;
    }

    /**
     * PaginationInfo 객체를 생성하는 생성자 (정렬 기준과 방향을 포함)
     * 
     * @param screenSize     한 페이지에 보여줄 데이터 수
     * @param blockSize      한 번에 보여줄 페이지 번호 수
     * @param page           현재 페이지 번호
     * @param orderBy        정렬 기준 컬럼명 (ex: mem_id, mem_name 등.. mapper에서 if 문으로 사용)
     * @param orderDirection 정렬 방향 (ASC(오름차순), DESC(내림차순))
     */
    public PaginationInfo(int screenSize, int blockSize, int page, String orderBy, String orderDirection) {
        this.screenSize = screenSize;
        this.blockSize = blockSize;
        this.page = page;
        this.orderBy = orderBy;
        this.orderDirection = orderDirection;
    }

    public int getOffset() {
        return (page - 1) * screenSize; // 데이터 조회 시 사용할 OFFSET 계산
    }

    public int getTotalPage() {
        return (totalCount + screenSize - 1) / screenSize; // 전체 페이지 수 계산
    }

    public int getEndPage() {
        return (page + blockSize - 1) / blockSize * blockSize; // 현재 페이지가 속한 블록의 마지막 페이지 번호 계산
    }

    public int getStartPage() {
        return getEndPage() - blockSize + 1; // 현재 페이지가 속한 블록의 시작 페이지 번호 계산
    }

}
