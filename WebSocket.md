# Web Socket

## 구현 계획

### 웹 소켓 서버

웹 소켓 서버는 Rest 모듈 서버에서 구현할 예정

### 흐름

```text

  [rest 서버 :8081]           [admin 서버 :8080]
  - WebSocket 서버            - 상담사 화면 (HTML/JS) 제공
  - 사용자 API                - 상담 목록, 채팅 UI 페이지
  - 채팅 메시지 처리
        ▲                            │
        │                            │ (브라우저가 admin에서 페이지 받고)
        └───── WebSocket 연결 ───────┘
               (JS 코드가 rest:8081/ws로 연결)
```

1. 클라이언트가 1대1 상담 페이지에 접속하면, 해당 클라이언트의 id를 기반으로 채팅 방을 생성 (DB에 저장)
2. 상담사는 상담 목록에서 해당 채팅 방을 선택하여 입장
3. 클라이언트와 상담사 간의 WebSocket 연결이 이루어지고, 실시간으로 메시지 송수신
4. 채팅 메시지는 rest 서버에서 처리하여 DB에 저장하거나, 상담사에게 전달
5. 채팅방에 다시 입장할 때, 이전 메시지 기록을 불러와서 보여줌

#### HandShake

JWT 토큰을 이용하여 WebSocket 연결 시 인증 처리

---

## DB 테이블 설계

### chat_room (채팅방)

| 컬럼       | 타입       | 설명               |
| ---------- | ---------- | ------------------ |
| room_id    | BIGINT PK  | 채팅방 ID          |
| user_id    | VARCHAR FK | 상담 요청한 사용자 |
| status     | VARCHAR    | OPEN / CLOSED      |
| created_at | DATETIME   | 생성 시각          |
| closed_at  | DATETIME   | 종료 시각          |

### chat_message (메시지)

| 컬럼        | 타입      | 설명         |
| ----------- | --------- | ------------ |
| message_id  | BIGINT PK | 메시지 ID    |
| room_id     | BIGINT FK | 채팅방 ID    |
| sender_type | VARCHAR   | USER / ADMIN |
| sender_id   | VARCHAR   | 발신자 ID    |
| content     | TEXT      | 메시지 내용  |
| sent_at     | DATETIME  | 전송 시각    |
| is_read     | BOOLEAN   | 읽음 여부    |

---

## STOMP 구조

### Subscribe (수신 경로)

| 경로                        | 대상            | 설명               |
| --------------------------- | --------------- | ------------------ |
| `/topic/chat.room.{roomId}` | 사용자 + 상담사 | 채팅방 메시지 수신 |
| `/user/queue/notification`  | 상담사          | 새 상담 요청 알림  |

### Publish (송신 경로)

| 경로                      | 설명             |
| ------------------------- | ---------------- |
| `/app/chat.send.{roomId}` | 메시지 전송      |
| `/app/chat.open`          | 채팅방 생성 요청 |

---

## 구현 파일 목록

### rest 모듈

| 파일                                   | 설명                                   |
| -------------------------------------- | -------------------------------------- |
| `config/WebSocketConfig.java`          | STOMP 설정, CORS, 엔드포인트 등록      |
| `config/WebSocketAuthInterceptor.java` | 연결 시 JWT 검증 채널 인터셉터         |
| `controller/ChatController.java`       | `@MessageMapping` 메시지 핸들러        |
| `service/ChatService.java`             | 채팅방 생성, 메시지 저장 비즈니스 로직 |
| `mapper/ChatMapper.java`               | chat_room, chat_message DB 쿼리        |

### admin 모듈

| 파일                                    | 설명                       |
| --------------------------------------- | -------------------------- |
| `controller/ChatConsoleController.java` | 상담 화면 페이지 반환      |
| `templates/chat/console.html`           | 상담사 채팅 UI (Thymeleaf) |

---

## 관리자 인증 처리

admin은 세션 기반 인증, rest WebSocket은 JWT 기반이므로 별도 처리 필요.

### 인증 흐름

```text
1. 상담사가 admin 서버에 로그인 (세션 발급)
2. 상담 화면 진입 시, admin 서버가 rest 서버에 단기 WS 토큰 요청
   POST rest:8081/api/internal/ws-token (서버 간 호출, 공유 시크릿 키)
3. rest 서버가 단기 JWT 반환 (유효시간 짧게, ex. 1시간)
4. admin 화면 JS가 해당 토큰을 헤더에 담아 WebSocket 연결
   ws://localhost:8081/ws?token={wsToken}
5. rest 서버의 WebSocketAuthInterceptor가 토큰 검증 후 연결 허용
```

### 핵심 포인트

- 관리자용 WS 토큰은 `ROLE_ADMIN` 클레임 포함
- rest 서버에서 `ROLE_ADMIN` 토큰은 `/user/queue/notification` 구독 허용
- 서버 간 호출 API(`/api/internal/ws-token`)는 외부 노출 차단 (공유 시크릿 헤더로 검증)
