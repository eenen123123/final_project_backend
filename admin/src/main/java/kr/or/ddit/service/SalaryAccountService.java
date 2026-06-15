package kr.or.ddit.service;

import java.util.List;
import java.util.Map;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.employee.SalaryAccountRowDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

/**
 * 급여 + 계좌 관리 서비스.
 * 조회/CSV용 목록 제공 및 결재 승인 후 급여·계좌 반영(집행)을 담당한다.
 */
public interface SalaryAccountService {

    /** 급여+계좌 목록 동적 검색 + 서버 페이징 (화면용) */
    PageResponse<SalaryAccountRowDto> searchSalaryAccountList(PaginationInfo<Map<String, Object>> paging);

    /** 급여+계좌 전체 목록 조회 (CSV 내보내기용) */
    List<SalaryAccountRowDto> getSalaryAccountList(Map<String, Object> params);

    /**
     * 결재 승인 후 급여·계좌 반영.
     * 기본급이 바뀐 경우에만 EMPLOYEE_SALARY 이력을 갱신하고, 계좌 정보는 upsert 한다.
     */
    void applySalaryAndAccount(String userId, Integer baseSalary,
                               String bankCd, String acntNo, String depositorNm,
                               String actorUserId);
}
