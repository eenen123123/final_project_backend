# StaffController - createEmployee 동작 정리

## 엔드포인트

| 항목 | 내용 |
|------|------|
| HTTP 메서드 | `POST` |
| URL | `/admin/employees` |
| 리턴 | `redirect:/admin/employees` |

---

## 파라미터

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `memberDto` | `MemberDto` | 회원 기본 정보 (ID, 비밀번호, 전화번호 등) |
| `employeeInfoDto` | `EmployeeInfoDto` | 직원 인사 정보 (부서, 직급, 입사일 등) |
| `employeeSalary` | `EmployeeSalaryDto` | 직원 급여 정보 |
| `profileImage` | `MultipartFile` | 프로필 이미지 (`tempuserProfile` 파라미터명, optional) |
| `principal` | `Principal` | Spring Security 세션에서 꺼낸 로그인 관리자 정보 |

---

## 처리 흐름

```
[폼 POST 요청]
      |
      v
1. ROLE 설정
   memberDto.userRole = "ROLE_ADMIN"
      |
      v
2. 비밀번호 암호화
   memberDto.userEnpswd = BCrypt 해시값
      |
      v
3. 등록자 ID 결정
   principal != null → principal.getName()
   principal == null → "SYSTEM" (기본값)
      |
      v
4. EmployeeInfoDto 보완
   rgtrId     = 로그인 관리자 ID
   lastMdfrId = 로그인 관리자 ID
      |
      v
5. EmployeeSalaryDto 보완
   userId    = memberDto.userId
   useYn     = "Y"
   applyYmd  = employeeInfoDto.joinYmd (입사일과 동일)
   rgtrId    = 로그인 관리자 ID
   lastMdfrId = 로그인 관리자 ID
      |
      v
6. 프로필 이미지 처리
   현재: 기본 이미지 경로 하드코딩 ("/images/default-profile.png")
   실제 파일 저장 로직은 미구현 상태
      |
      v
7. 전화번호 하이픈 제거
   "010-1234-5678" → "01012345678"
      |
      v
8. staffService.registerEmployee() 호출
   트랜잭션으로 3개 테이블에 일괄 INSERT
   ┣ MEMBER       ← memberDto
   ┣ EMPLOYEE_INFO ← employeeInfoDto
   └ EMPLOYEE_SALARY ← employeeSalary
      |
      v
redirect:/admin/employees
```

---

## DB INSERT 대상 테이블

| 순서 | 테이블 | DTO |
|------|--------|-----|
| 1 | `MEMBER` | `MemberDto` |
| 2 | `EMPLOYEE_INFO` | `EmployeeInfoDto` |
| 3 | `EMPLOYEE_SALARY` | `EmployeeSalaryDto` |

세 테이블은 `staffService.registerEmployee()` 안에서 **하나의 트랜잭션**으로 처리됨.  
하나라도 실패하면 전체 롤백.

---

### 트랜잭션 롤백 흐름

```
[클라이언트 / 컨트롤러]
       │
       ▼ (1. '통합 등록' 메서드 호출)
[스프링 트랜잭션 프록시] ──► (2. 하나의 트랜잭션 시작)
       │
       ▼ (3. 서비스 구현체 내부 진입)
[StaffServiceImpl.registerEmployee]
       │
       ├─► 4. staffMapper.insertEmployee(memberDto);      ──► [성공]
       │
       ├─► 5. staffMapper.insertEmployeeInfo(infoDto);    ──► [💥 여기서 에러 발생!]
       │
       └─► 6. (급여 저장은 실행조차 되지 않음)
       │
       ▼ (7. 에러가 프록시로 전파됨)
[스프링 트랜잭션 프록시] ──► (8. 4번에서 성공했던 직원 등록까지 '전체 Rollback' 감행)
       │
       ▼
[최종 결과: DB에는 아무 데이터도 남지 않는 안전한 상태 유지]
```

---

## 현재 미구현 / 추후 작업 항목

| 항목 | 현황 |
|------|------|
| 유효성 검증 (`@Valid`, `BindingResult`) | 코드는 존재하나 주석 처리됨 |
| 프로필 이미지 실제 저장 | 기본 이미지 경로로 하드코딩 중 |
| 프로필 이미지 파라미터명 | `tempuserProfile` (임시) → `userProfile` 로 변경 예정 |
