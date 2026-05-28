package kr.or.ddit.finalProject.service.staff;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import kr.or.ddit.finalProject.dto.employee.DepartmentDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeDetailDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeInfoDto;
import kr.or.ddit.finalProject.dto.employee.EmployeeSalaryDto;
import kr.or.ddit.finalProject.dto.employee.JobGradeDto;
import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.StaffMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaffServiceImpl implements StaffService{

    @Autowired
    PasswordEncoder passwordEncoder;

    private final StaffMapper staffMapper;

    // 부서 리스트 조회
    @Override
    public List<DepartmentDto> retrieveDepartmentList() {
        return staffMapper.selectDepartmentList();
    }

    // 직급 리스트 조회
    @Override
    public List<JobGradeDto> retrieveJobGradeList() {
        return staffMapper.selectJobGradeList();
    }

    // 직원 등록, 직원 정보 저장, 직원 급여 정보 저장
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerEmployee(MemberDto memberDto, EmployeeInfoDto employeeInfoDto, EmployeeSalaryDto employeeSalaryDto, MultipartFile profileImage, String loginAdminId) {

        // 1. ROLE 및 기본 프로필 설정
        memberDto.setUserRole("ROLE_ADMIN"); // 기본적으로 ROLE_ADMIN으로 설정
        memberDto.setUserProfile("/images/default-profile.png"); // 기본 프로필 이미지 경로 설정

        // 2. 비밀번호 암호화
        memberDto.setUserEnpswd(passwordEncoder.encode(memberDto.getUserEnpswd()));

        // 3. 연락처 하이픈 제거
        if (memberDto.getUserTelno() != null) {
            String cleanTelno = memberDto.getUserTelno().replaceAll("-","");
            memberDto.setUserTelno(cleanTelno);
        }

        // 4. ID 중복 체크
        String generatedId = memberDto.getUserId();// 화면에서 넘어온 최초 생성 ID (예: 202605KH07)
        String baseId = generatedId.substring(0, 10); // "년월+초성" 패턴 분리 (예: 202605KH)

        boolean isDuplicate = true;
        int safetyCount = 0; // 무한 루프 방지 방어벽

        while (isDuplicate && safetyCount < 100) {
            // DB에 현재 ID가 이미 존재하는지 최종 확인
            int count = staffMapper.checkIdExists(generatedId);

            if (count == 0) {
                // 중복이 없다면 이 ID를 최종 사용하기로 확정하고 루프 탈출
                isDuplicate = false;
            } else {
                // 중복이 발생했다면, DB에서 해당 패턴의 최신 MAX ID를 다시 조회합니다.
                String maxId = staffMapper.selectMaxUserId(baseId);
                
                // 조회된 최고 높은 ID의 맨 뒤 2자리를 잘라 숫자로 바꾼 뒤 1을 더해줍니다 (++)
                String lastTwoDigits = maxId.substring(maxId.length() - 2);
                int nextSerialInt = Integer.parseInt(lastTwoDigits) + 1;
                
                // 다시 2자리 문자열 포맷으로 변환하여 베이스 아이디 뒤에 결합합니다.
                String nextSerial = String.format("%02d", nextSerialInt);
                generatedId = baseId + nextSerial; // 예: 202605KH08로 안전하게 업데이트
                
                safetyCount++;
            }
        }

        // 100회 시도 후에도 중복 해소 실패 → 예외 발생
        if (isDuplicate) {
            log.error("[registerEmployee] ID 생성 실패 - 100회 시도 소진. baseId={}", baseId);
            throw new FinalProjectException(ErrorCode.EMPLOYEE_ID_GENERATE_FAILED);
        }

        // 최종적으로 중복이 없는 ID가 확보된 상태에서 memberDto에 주입합니다.
        memberDto.setUserId(generatedId);

        // 5. EmployeeInfoDto에 데이터 넣기
        employeeInfoDto.setRgtrId(loginAdminId); // 최초등록자ID -> 현재 로그인한 관리자 ID
        employeeInfoDto.setLastMdfrId(loginAdminId); // 최종등록자ID -> 현재 로그인한 관리자 ID

        // 6. EmployeeSalaryDto 데이터 넣기
        employeeSalaryDto.setUserId(memberDto.getUserId());
        employeeSalaryDto.setUseYn("Y"); // 첫 등록이므로 현재 사용 여부는 무조건 'Y'로 설정
        employeeSalaryDto.setApplyYmd(employeeInfoDto.getJoinYmd()); // 급여 적용 시작일은 입사일과 동일하게 설정
        employeeSalaryDto.setRgtrId(loginAdminId); // 최초등록자ID -> 현재 로그인한 관리자 ID
        employeeSalaryDto.setLastMdfrId(loginAdminId); // 최종등록자ID -> 현재 로그인한 관리자 ID


        log.info("등록할 직원 정보: {}", memberDto);
        log.info("등록할 직원 상세 정보: {}", employeeInfoDto);
        log.info("등록할 직원 급여 정보: {}", employeeSalaryDto);

        /**
         * 일괄 트랜잭션 등록 처리
         * 1. 직원 등록 (MemberDto) -> 회원 마스터 테이블(MEMBER)에 INSERT
         * 2. 직원 상세 정보 저장 (EmployeeInfoDto) -> 직원 인사 관리 테이블(EMPLOYEE_INFO)에 INSERT
         * 3. 직원 급여 정보 저장 (EmployeeSalaryDto) -> 직원 급여 테이블(EMPLOYEE_SALARY)에 INSERT
         */
        try {
            staffMapper.insertEmployee(memberDto);
            staffMapper.insertEmployeeInfo(employeeInfoDto);
            staffMapper.insertEmployeeSalary(employeeSalaryDto);
        } catch (DataAccessException e) {
            log.error("[registerEmployee] DB INSERT 실패. userId={}, cause={}", memberDto.getUserId(), e.getMessage());
            throw new FinalProjectException(ErrorCode.EMPLOYEE_REGISTER_FAILED, e);
        }
    }

    // 직원 리스트 조회
    @Override
    public List<EmployeeDetailDto> retrieveEmployeeList() {
        return staffMapper.selectEmployeeList();
    }

    // 아이디 중복 자동 순번 발급 및 중복 회피
    @Override
    public String getNextAvailableId(String baseId, String defaultSerial) {
        // DB에서 가장 큰 ID 조회 (예: "202605KH07")
        String maxId = staffMapper.selectMaxUserId(baseId);
        
        if (maxId == null) {
            // 기존에 등록된 동일 패턴의 ID가 전혀 없다면 처음 제안된 기본값("202605KH07") 그대로 반환
            return baseId + defaultSerial;
        }
        
        // 가장 뒤의 2자리 일련번호를 잘라내어 숫자로 변환 후 ++ 해줍니다.
        try {
            String lastTwoDigits = maxId.substring(maxId.length() - 2);
            int nextSerialInt = Integer.parseInt(lastTwoDigits) + 1;
            String nextSerial = String.format("%02d", nextSerialInt);
            return baseId + nextSerial;
        } catch (NumberFormatException e) {
            log.error("[getNextAvailableId] ID 끝 2자리 숫자 파싱 실패. maxId={}", maxId);
            throw new FinalProjectException(ErrorCode.EMPLOYEE_ID_GENERATE_FAILED, e);
        }
    }

}
