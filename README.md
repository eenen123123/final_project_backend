# Final Project - Backend

## 1. 프로젝트 구조

```text
backend
├── admin (관리자 모듈, 타임리프 + 스프링)
├── common (공통 모듈)
└── rest (REST API 모듈, React와 통신)
```

## 2. 프로젝트 실행

- 관리자 페이지 개발 시 admin 모듈을 실행

  ```bash
   cd admin
   ./mvnw spring-boot:run
  ```

- REST API 개발 시 rest 모듈을 실행

  ```bash
  cd rest
  ./mvnw spring-boot:run
  ```

- 공통 모듈( common )은 admin과 rest에서 모두 참조하므로, 별도의 실행이 필요하지 않음

## 3. 접근 주소

- 관리자 페이지: `http://localhost:8080`
- REST API: `http://localhost:8081`

> [!IMPORTANT]
>
> ## 4. 환경 변수 설정
>
> `.env.example` 파일을 복사하여 `.env` 파일을 생성한 후, 필요한 환경 변수를 설정합니다.
>
> ```bash
> # .env
>
> db_host=192.168.0.1
> db_username=foo
> db_password=bar
> ```

## 5. 페이징 처리

[common/src/main/java/kr/or/ddit/finalProject/paging/PaginationInfo.java](common/src/main/java/kr/or/ddit/finalProject/paging/PaginationInfo.java)

위의 클래스는 페이징 처리를 위한 정보를 담고 있습니다.

아래와 같이 두개의 생성자가 제공되며, 첫 번째 생성자는 기본적인 페이징 정보만을 설정하는 반면, 두 번째 생성자는 정렬 기준과 방향까지 포함하여 설정할 수 있습니다.

정렬과 관련된 필드인 `orderBy`와 `orderDirection`은 예를 들어, 회원 목록을 반환할 때 `mem_id`를 기준으로 오름차순으로 정렬하려면, `orderBy`에 "mem_id"를, `orderDirection`에 "ASC"를 설정하면 됩니다.

### 5-1. PaginationInfo 클래스의 생성자 예시

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

### 5-2. MyBatis Mapper XML에서 정렬 기준과 방향, 페이징을 사용하는 예시

> temp 패키지는 페이징 처리 테스트를 위한 임시 패키지입니다. 실제로는 도메인 객체와 관련된 패키지에서 사용해야 해요.

```xml
    <!-- 검색과 정렬을 할 수 있도록 조각을 정의 -->
    <sql id="detailConditionFragment">
        <trim prefix="where" prefixOverrides="and">
            <if test="detailCondition != null ">
                <if test="detailCondition.memName != null and detailCondition.memName != ''">
                    AND instr (m.mem_name, #{detailCondition.memName}) &gt; 0
                </if>
                <if test="detailCondition.memAdd1 != null and detailCondition.memAdd1 != ''">
                    AND
                    ( instr (m.mem_add1, #{detailCondition.memAdd1}) &gt; 0
                    OR instr (m.mem_add2, #{detailCondition.memAdd1}) &gt; 0
                    )
                </if>
            </if>
        </trim>
    </sql>

    <sql id="orderFragment">
        <if test="orderBy != null and orderBy != ''">
            order by ${orderBy} ${orderDirection}
        </if>
    </sql>


    <!-- 페이징 처리를 위한 쿼리 -->
    <select id="selectMemberDtoForPagingTestList" resultType="kr.or.ddit.finalProject.paging.temp.MemberDtoForPagingTest">
        select * from (
            select mem_id, mem_name, mem_mail, mem_add1, mem_add2, mem_zip, mem_hp
            from member m
                <include refid="detailConditionFragment"/>
                <include refid="orderFragment"/>
        )
        offset #{offset} rows fetch next #{screenSize} rows only
    </select>

    <select id="getTotalMemberCount" resultType="int">
        SELECT COUNT(*) FROM MEMBER m
        <include refid="detailConditionFragment"/>
    </select>
```

## 6. 예외 처리

- ### 커스텀 예외 클래스

  [common/src/main/java/kr/or/ddit/finalProject/exception/FinalProjectException.java](common/src/main/java/kr/or/ddit/finalProject/exception/FinalProjectException.java)

  위 클래스는 우리 프로젝트에서 발생할 수 있는 예외 상황을 나타내는 최상위 커스텀 예외 클래스입니다. 이 클래스를 상속하여 다양한 예외 상황에 대한 구체적인 예외 클래스를 정의할 수 있습니다.

  상속 받아 정의할 클래스는 도메인 객체와 관련된 예외 상황만 정의하고,
  구체적인 예외 상황에 대한 메시지는 ErrorCode ENUM에서 정의된 메시지를 사용하여 생성자에서 전달하는 방식으로 구현합니다.

  ```java
  /**
   * 사용자 관련 예외를 처리하기 위한 커스텀 예외 클래스
   */
  public class UserException extends FinalProjectException {
      public UserException(ErrorCode errorCode) {
          super(errorCode);
      }
      public UserException(ErrorCode errorCode, Throwable cause) {
          super(errorCode, cause);
      }
  }

  // 사용 예시 (예외 상황에 따라 적절한 에러 코드를 전달하여 예외 객체를 생성)
  // 예를 들어, 사용자가 존재하지 않는 경우에 대한 예외 처리
  throw new UserException(ErrorCode.USER_NOT_FOUND);

  // ErrorCode.java 파일에서 USER_NOT_FOUND 에러 코드는 다음과 같이 정의되어 있습니다.
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  ```

- ### 에러 코드 ENUM

  [common/src/main/java/kr/or/ddit/finalProject/exception/ErrorCode.java](common/src/main/java/kr/or/ddit/finalProject/exception/ErrorCode.java)

  위 ENUM에서 정의된 에러 코드는 우리 프로젝트에서 발생할 수 있는 다양한 예외 상황을 나타냅니다

- ### REST API 예외 처리 핸들러

  [rest/src/main/java/kr/or/ddit/exception/RestExceptionHandler.java](rest/src/main/java/kr/or/ddit/exception/RestExceptionHandler.java)

  위 클래스는 REST API에서 발생하는 예외를 처리하는 핸들러입니다. `@RestControllerAdvice` 어노테이션을 사용하여 모든 REST 컨트롤러에서 발생하는 예외를 전역적으로 처리할 수 있습니다.

  이 핸들러가 있어 컨트롤러 메소드에서 예외를 따로 처리하지 않고 어느 레이어에서든 예외를 던지면, 해당 예외가 이 핸들러로 전달되어 적절한 HTTP 상태 코드와 메시지를 포함한 일관된 응답이 반환됩니다.

  단, 이 핸들러는 Rest API 모듈에서만 그리고 FinalProjectException을 상속한 예외 클래스에 대해서만 적용됩니다.

## 7. JWT
