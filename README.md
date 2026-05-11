# Final Project - Backend

## 프로젝트 구조

```
backend
├── admin (관리자 모듈, 타임리프 + 스프링)
├── common (공통 모듈)
└── rest (REST API 모듈, React와 통신)
```

## 프로젝트 실행

- 관리자 페이지 개발 시 admin 모듈을 실행
- REST API 개발 시 rest 모듈을 실행
- 공통 모듈은 admin과 rest에서 모두 참조하므로, 별도의 실행이 필요하지 않음

## 접근 주소

- 관리자 페이지: `http://localhost:8080`
- REST API: `http://localhost:8081`
