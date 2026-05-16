-- 사용자관리 테이블 생성
CREATE TABLE MEMBER (
    USER_ID          VARCHAR2(20)   NOT NULL,                          -- 사용자ID (PK) | 20자리 이내 문자
    USER_ENPSWD      VARCHAR2(256)  NOT NULL,                          -- 사용자비밀번호 | 암호화 저장 필수
    USER_NM          VARCHAR2(100)  NOT NULL,                          -- 사용자명
    USER_ENRRNO      VARCHAR2(256)  NULL,                              -- 암호화주민등록번호 | 암호화 필수, 원문 저장 금지
    USER_GNDR_CD     CHAR(1)        NULL,                              -- 성별코드 | M:남, F:여, U:미확인
    USER_BRDT        CHAR(8)        NULL,                              -- 사용자생년월일 | YYYYMMDD (0001~9999)
    JOIN_DT          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL, -- 가입일시
    USER_TELNO       VARCHAR2(11)   NULL,                              -- 사용자전화번호 | (9)99-(9)999-9999 형식
    USER_EML_ADDR    VARCHAR2(320)  NULL,                              -- 사용자이메일주소
    USER_ZIP         CHAR(5)        NULL,                              -- 사용자우편번호 | 5자리 문자, ≤99999
    USER_ADDR        VARCHAR2(200)  NULL,                              -- 사용자주소
    USER_DADDR       VARCHAR2(200)  NULL,                              -- 사용자상세주소
    PRFL_IMG_PATH_NM VARCHAR2(300)  NULL,                              -- 프로필이미지경로명
    REG_DT           TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL, -- 등록일시
    MDFCN_DT         TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL, -- 수정일시

    -- Primary Key
    CONSTRAINT PK_MEMBER PRIMARY KEY (USER_ID)
);

-- 테이블 코멘트
COMMENT ON TABLE  MEMBER                  IS '사용자관리';

-- 컬럼 코멘트
COMMENT ON COLUMN MEMBER.USER_ID          IS '사용자ID';
COMMENT ON COLUMN MEMBER.USER_ENPSWD      IS '사용자비밀번호 (암호화 저장)';
COMMENT ON COLUMN MEMBER.USER_NM          IS '사용자명';
COMMENT ON COLUMN MEMBER.USER_ENRRNO      IS '암호화주민등록번호 (암호화 필수, 원문 저장 금지)';
COMMENT ON COLUMN MEMBER.USER_GNDR_CD     IS '성별코드 (M:남, F:여, U:미확인)';
COMMENT ON COLUMN MEMBER.USER_BRDT        IS '사용자생년월일 (YYYYMMDD)';
COMMENT ON COLUMN MEMBER.JOIN_DT          IS '가입일시';
COMMENT ON COLUMN MEMBER.USER_TELNO       IS '사용자전화번호';
COMMENT ON COLUMN MEMBER.USER_EML_ADDR    IS '사용자이메일주소';
COMMENT ON COLUMN MEMBER.USER_ZIP         IS '사용자우편번호';
COMMENT ON COLUMN MEMBER.USER_ADDR        IS '사용자주소';
COMMENT ON COLUMN MEMBER.USER_DADDR       IS '사용자상세주소';
COMMENT ON COLUMN MEMBER.PRFL_IMG_PATH_NM IS '프로필이미지경로명';
COMMENT ON COLUMN MEMBER.REG_DT           IS '등록일시';
COMMENT ON COLUMN MEMBER.MDFCN_DT         IS '수정일시';