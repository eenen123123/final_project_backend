-- 접속 이력 테이블 생성
CREATE TABLE ADMINLOGIN_LOG (
    LOG_ID      NUMBER          NOT NULL,                               -- 로그ID (PK)
    USER_ID     VARCHAR2(20)    NOT NULL,                               -- 사용자ID (FK → MEMBER)
    LOGIN_IP    VARCHAR2(45)    NULL,                                   -- 접속 IP (IPv6 대비 45자)
    LOGIN_DT    TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,          -- 로그인 시각
    LOGOUT_DT   TIMESTAMP       NULL,                                   -- 로그아웃 시각 (NULL = 접속 중 or 세션 만료)
    SESSION_ID  VARCHAR2(100)   NULL,                                   -- 세션 ID

    -- Primary Key
    CONSTRAINT PK_ADMINLOGIN_LOG PRIMARY KEY (LOG_ID),

    -- Foreign Key
    CONSTRAINT FK_ADMINLOGIN_LOG_MEMBER FOREIGN KEY (USER_ID) REFERENCES MEMBER(USER_ID)
);

-- PK 시퀀스
CREATE SEQUENCE ADMINLOGIN_LOG_SEQ
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 조회 성능 인덱스
CREATE INDEX IDX_ADMINLOGIN_LOG_USER_ID ON ADMINLOGIN_LOG(USER_ID);
CREATE INDEX IDX_ADMINLOGIN_LOG_LOGIN_DT ON ADMINLOGIN_LOG(LOGIN_DT DESC);
