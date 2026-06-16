package kr.or.ddit.finalProject.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import kr.or.ddit.finalProject.dto.employee.EmployeeBankAccountDto;
import kr.or.ddit.finalProject.dto.employee.SalaryAccountRowDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

/**
 * 급여 + 계좌 관리 전용 매퍼.
 * 급여(EMPLOYEE_SALARY) 변경은 기존 StaffMapper 메서드를 재사용하고,
 * 여기서는 화면 목록 조회와 계좌(EMPLOYEE_BANK_ACCOUNT) upsert만 담당한다.
 */
@Mapper
public interface SalaryAccountMapper {

    /** 급여+계좌 목록 동적 검색 + 서버 페이징 */
    List<SalaryAccountRowDto> searchSalaryAccountList(PaginationInfo<Map<String, Object>> paging);

    /** 급여+계좌 전체 건수 (페이지 버튼 계산용) */
    int countSalaryAccountList(PaginationInfo<Map<String, Object>> paging);

    /** 급여+계좌 통합 목록 전체 조회 (CSV 내보내기용, 페이징 없음) */
    List<SalaryAccountRowDto> selectSalaryAccountList(Map<String, Object> params);

    /** 단일 직원 계좌 조회 */
    EmployeeBankAccountDto selectBankAccount(String userId);

    /** 계좌 upsert (있으면 UPDATE, 없으면 INSERT) */
    void mergeBankAccount(EmployeeBankAccountDto dto);
}
