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

## 환경 변수 설정

`.env.example` 파일을 복사하여 `.env` 파일을 생성한 후, 필요한 환경 변수를 설정합니다.

```bash
# .env

db_host=192.168.35.150
db_username=foo
db_password=bar
```

## 페이징 처리

[common/src/main/java/kr/or/ddit/finalProject/paging/PaginationInfo.java](common/src/main/java/kr/or/ddit/finalProject/paging/PaginationInfo.java)

위의 클래스는 페이징 처리를 위한 정보를 담고 있습니다.

아래와 같이 두개의 생성자가 제공되며, 첫 번째 생성자는 기본적인 페이징 정보만을 설정하는 반면, 두 번째 생성자는 정렬 기준과 방향까지 포함하여 설정할 수 있습니다.

정렬과 관련된 필드인 `orderBy`와 `orderDirection`은 예를 들어, 회원 목록을 반환할 때 `mem_id`를 기준으로 오름차순으로 정렬하려면, `orderBy`에 "mem_id"를, `orderDirection`에 "ASC"를 설정하면 됩니다.

```java
/**
 * PaginationInfo 객체를 생성하는 생성자
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
 * @param orderBy        정렬 기준 컬럼명 (ex: mem_id, mem_name 등.. mapper에서 if 문으로 사용됨)
 * @param orderDirection 정렬 방향 (ASC(오름차순), DESC(내림차순))
 */
public PaginationInfo(int screenSize, int blockSize, int page, String orderBy, String orderDirection) {
    this.screenSize = screenSize;
    this.blockSize = blockSize;
    this.page = page;
    this.orderBy = orderBy;
    this.orderDirection = orderDirection;
}

```
