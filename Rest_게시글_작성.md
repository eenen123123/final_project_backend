# REST 모듈 게시글 작성 로직

> 작성일: 2026-06-02

---

## 1. 전체 흐름

```
[React]
 TipTapEditor에서 글 작성
 → 이미지 선택 시 즉시 파일 서버 업로드 → fileId를 노드 attrs에 저장
 → 제출 직전 stripBlobUrls()로 blob URL 제거
 → extractFileIds()로 fileId 목록 추출
 → POST /api/posts/{type} 전송
     body: { postSj, postCn: JSONContent, fileIds: number[] }

[REST 서버]
 1. @Valid로 요청 형식 검증 (postSj NotBlank, postCn NotNull, fileIds Size max=10)
 2. fileIds 소유자 배치 검증 (COUNT 쿼리 1번)
 3. postCn(JsonNode) → JSON 문자열 직렬화
 4. BOARD INSERT (selectKey로 postSn 채번)
 5. fileIds의 CTX_TYPE='POST', CTX_ID=postSn UPDATE
 6. postSn 반환
```

---

## 2. 관련 파일

| 역할            | 파일                                                                                                                           |
| --------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| 요청 DTO        | [EditorPostRequestDto.java](../../../backend/common/src/main/java/kr/or/ddit/finalProject/dto/board/EditorPostRequestDto.java) |
| 서비스          | [RestPostService.java](../../../backend/rest/src/main/java/kr/or/ddit/service/board/RestPostService.java)                      |
| 테스트 컨트롤러 | [ExampleRestController.java](../../../backend/rest/src/main/java/kr/or/ddit/controller/ExampleRestController.java)             |
| 게시글 매퍼     | [BoardMapper.java](../../../backend/common/src/main/java/kr/or/ddit/finalProject/mapper/board/BoardMapper.java)                |
| 파일 매퍼       | [FileMapper.java](../../../backend/common/src/main/java/kr/or/ddit/finalProject/mapper/FileMapper.java)                        |
| 에디터 컴포넌트 | [TipTapEditor.tsx](https://github.com/eenen123123/final_project_frontend/blob/main/src/components/TipTapEditor.tsx)            |
| 파일 API 유틸   | [fileApi.ts](https://github.com/eenen123123/final_project_frontend/blob/main/src/api/fileApi.ts)                               |
|                 |                                                                                                                                |

---

## 3. 요청 DTO 구조

```java
// EditorPostRequestDto
String postSj;          // @NotBlank — 제목
JsonNode postCn;        // @NotNull  — TipTap JSONContent 객체
List<Long> fileIds;     // @Size(max=10) — 에디터 내 이미지의 fileServerId 목록
```

프론트에서 보내는 실제 JSON:

```json
{
    "postSj": "제목",
    "postCn": {
        "type": "doc",
        "content": [
            {
                "type": "paragraph",
                "content": [{ "type": "text", "text": "내용" }]
            },
            {
                "type": "image",
                "attrs": { "fileId": 123, "src": null, "alt": "사진" }
            }
        ]
    },
    "fileIds": [123]
}
```

---

## 4. 서비스 사용법 (컨트롤러에서)

```java
// boardTypeCd는 호출하는 컨트롤러에서 맥락에 맞게 전달
long postSn = restPostService.createPost(req, "BOARD_TYPE_CD", authentication);
```

`boardTypeCd`는 BOARD 테이블의 `BOARD_TYPE_CD` 컬럼 값. 어떤 게시판 타입인지 공통코드 테이블(CL_CODE) 확인해서 넣기.

---

## 5. 게시글 조회

```java
// 서비스
Map<String, Object> post = restPostService.getPost(postSn);
// 반환: { postSn, postSj, postCn(JsonNode) }
```

`postCn`은 DB에 저장된 JSON 문자열을 Jackson이 파싱해서 `JsonNode`로 반환.
프론트에서는 응답의 `postCn`을 그대로 TipTap `initialContent`에 넣으면 됨.

```ts
const res = await api.get(`/api/posts/${postSn}`);
<TipTapEditor initialContent={res.data.postCn} editable={false} />
```

---

## 6. 프론트 제출 로직 핵심

```ts
const cleanContent = stripBlobUrls(content); // blob URL 제거 (에디터 상태는 유지)
const fileIds = extractFileIds(cleanContent); // image 노드에서 fileId 추출

await api.post("/api/posts/...", {
    postSj: title,
    postCn: cleanContent,
    fileIds,
});
```

**제출 전 가드 조건:**

- `hasPendingUploads === true` → 버튼 비활성화 (이미지 업로드 중)
- 업로드 실패한 이미지 노드(`uploadStatus: 'error'`)가 있으면 제출 막기 권장

---

## 7. 이미지 파일 컨텍스트 처리

게시글 이미지는 업로드 시 `ctxType: "POST"`, `ctxId: "0"` 으로 임시 등록됨.
게시글 저장 완료 후 서버에서 `CTX_ID`를 실제 `postSn`으로 UPDATE함.

```sql
-- RestPostService 내부에서 자동 처리
UPDATE CMMT_ATCH_FILE_DTL
SET CTX_TYPE = 'POST', CTX_ID = #{postSn}
WHERE FILE_SERVER_ID IN (fileIds...)
```

읽기 시 이미지 접근: TipTap `ImageNodeView`가 마운트 시점에 `POST /api/files/{fileId}/token` 호출 → pre-signed URL(60초) 발급 → `<img src>` 세팅.

---

## 8. 예외 처리

| 상황                     | 던지는 예외              | HTTP |
| ------------------------ | ------------------------ | ---- |
| fileIds 중 소유자 불일치 | `FILE_ACCESS_DENIED`     | 403  |
| postCn JSON 직렬화 실패  | `JSON_PROCESSING_FAILED` | 500  |
| 게시글 없음              | `POST_NOT_FOUND`         | 404  |
| @Valid 검증 실패         | (핸들러가 자동 처리)     | 400  |

예외는 모든 케이스를 `{ status, message }` 형태로 통일해서 반환.

---

## 9. 다른 게시판 타입에 응용할 때

`RestPostService.createPost(req, boardTypeCd, auth)`를 그대로 재사용하고 `boardTypeCd`만 바꾸면 됨.

추가 필드가 필요한 경우 (예: QnA의 `qnaCtgCd`):

1. 별도 요청 DTO 작성 (`EditorPostRequestDto` 상속 또는 래핑)
2. 서비스에서 `createPost()` 호출 후 반환된 `postSn`으로 서브 테이블 INSERT 추가

```java
long postSn = restPostService.createPost(req, "QNA", auth);
qnaMapper.insertQna(QnaDto.builder().postSn(postSn).qnaCtgCd(req.getQnaCtgCd()).build());
```

---

## 10. 주의사항

- `postCn`은 DB에 **문자열(CLOB)**로 저장됨. `BOARD.POST_CN` 컬럼이 CLOB인지 확인 필요.
- 이미지 업로드는 에디터에서 선택하는 순간 발생. 글을 저장하지 않고 나가면 파일 서버에 고아 파일이 생김 (일반적으로 허용되는 trade-off).
- `fileIds`가 빈 배열이면 소유자 검증 및 CTX 업데이트 쿼리 모두 생략됨 (이미지 없는 글).
- TipTap JSON에 `uploadStatus` 필드는 저장하지 않아도 무방하나, `stripBlobUrls`는 반드시 호출해야 blob URL이 DB에 들어가지 않음.

---

## 관련 노트

- [[에디터_이미지_업로드]] — TipTap 이미지 업로드 전체 설계
- [[파일_서버]] — 파일 서버 아키텍처 및 pre-signed URL 패턴
