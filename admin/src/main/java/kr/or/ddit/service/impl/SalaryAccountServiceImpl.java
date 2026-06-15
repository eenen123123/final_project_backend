package kr.or.ddit.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.employee.EmployeeBankAccountDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeSalaryDto;
import kr.or.ddit.finalProject.dto.employee.SalaryAccountRowDto;
import kr.or.ddit.finalProject.mapper.SalaryAccountMapper;
import kr.or.ddit.finalProject.mapper.StaffMapper;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.service.SalaryAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryAccountServiceImpl implements SalaryAccountService {

    private final SalaryAccountMapper salaryAccountMapper;
    private final StaffMapper staffMapper; // 급여 이력 메서드 재사용 (공용 코드 미수정)

    @Override
    public PageResponse<SalaryAccountRowDto> searchSalaryAccountList(PaginationInfo<Map<String, Object>> paging) {
        List<SalaryAccountRowDto> items = salaryAccountMapper.searchSalaryAccountList(paging);
        int totalCount = salaryAccountMapper.countSalaryAccountList(paging);
        return new PageResponse<>(items, totalCount);
    }

    @Override
    public List<SalaryAccountRowDto> getSalaryAccountList(Map<String, Object> params) {
        return salaryAccountMapper.selectSalaryAccountList(params);
    }

    @Override
    @Transactional
    public void applySalaryAndAccount(String userId, Integer baseSalary,
                                      String bankCd, String acntNo, String depositorNm,
                                      String actorUserId) {

        // 1. 기본급 변경 처리 — 값이 넘어왔고 현재 급여와 다를 때만 이력 갱신
        if (baseSalary != null) {
            EmployeeSalaryDto current = staffMapper.selectCurrentSalary(userId);
            boolean changed = current == null || !baseSalary.equals(current.getBaseSalary());
            if (changed) {
                staffMapper.deactivateCurrentSalary(userId);

                EmployeeSalaryDto salary = EmployeeSalaryDto.builder()
                    .userId(userId)
                    .baseSalary(baseSalary)
                    .applyYmd(LocalDate.now())
                    .useYn("Y")
                    .rgtrId(actorUserId)
                    .lastMdfrId(actorUserId)
                    .build();
                staffMapper.insertEmployeeSalary(salary);
                log.info("[SalaryAccount] 급여 이력 갱신: userId={}, baseSalary={}", userId, baseSalary);
            }
        }

        // 2. 계좌 정보 upsert — 은행/계좌/예금주가 모두 채워졌을 때만 반영
        if (isNotBlank(bankCd) && isNotBlank(acntNo) && isNotBlank(depositorNm)) {
            EmployeeBankAccountDto account = EmployeeBankAccountDto.builder()
                .userId(userId)
                .bankCd(bankCd)
                .acntNo(acntNo.trim())
                .depositorNm(depositorNm.trim())
                .rgtrId(actorUserId)
                .lastMdfrId(actorUserId)
                .build();
            salaryAccountMapper.mergeBankAccount(account);
            log.info("[SalaryAccount] 계좌 정보 반영: userId={}, bankCd={}", userId, bankCd);
        }
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}
