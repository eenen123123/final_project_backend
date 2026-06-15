-- ============================================================
-- 직원 급여 계좌 테이블
--   - 직원당 급여 입금 계좌 1건 (USER_ID 1:1)
--   - 외부 급여지급 프로그램이 은행/계좌/예금주를 그대로 가져가 사용
--   - 계좌번호(ACNT_NO)는 앞자리 0·하이픈 보존을 위해 반드시 문자열(VARCHAR2)
--   - 은행(BANK_CD)은 공통코드 분류 702(은행)를 참조
-- ============================================================

CREATE TABLE EMPLOYEE_BANK_ACCOUNT (
    USER_ID       VARCHAR2(20)  NOT NULL,                      -- 직원 ID (PK, EMPLOYEE_INFO 참조)
    BANK_CD       VARCHAR2(10)  NOT NULL,                      -- 은행 (공통코드 702)
    ACNT_NO       VARCHAR2(50)  NOT NULL,                      -- 계좌번호 (문자열)
    DEPOSITOR_NM  VARCHAR2(100) NOT NULL,                      -- 예금주명
    USE_YN        CHAR(1)       DEFAULT 'Y' NOT NULL,          -- 사용 여부 (Y/N)
    RGTR_ID       VARCHAR2(20),                                -- 등록자 ID
    LAST_MDFR_ID  VARCHAR2(20),                                -- 최종 수정자 ID
    REG_DT        TIMESTAMP(6)  DEFAULT SYSTIMESTAMP NOT NULL, -- 등록일시
    MDFCN_DT      TIMESTAMP(6)  DEFAULT SYSTIMESTAMP NOT NULL, -- 수정일시
    CONSTRAINT PK_EMPLOYEE_BANK_ACCOUNT     PRIMARY KEY (USER_ID),
    CONSTRAINT CK_EMP_BANK_ACCOUNT_USE_YN   CHECK (USE_YN IN ('Y', 'N')),
    CONSTRAINT FK_EMP_INFO_TO_BANK_ACCOUNT  FOREIGN KEY (USER_ID)
        REFERENCES EMPLOYEE_INFO (USER_ID)
);

COMMENT ON TABLE  EMPLOYEE_BANK_ACCOUNT              IS '직원 급여 계좌 테이블';
COMMENT ON COLUMN EMPLOYEE_BANK_ACCOUNT.USER_ID      IS '직원 ID, EMPLOYEE_INFO 참조 (PK)';
COMMENT ON COLUMN EMPLOYEE_BANK_ACCOUNT.BANK_CD      IS '은행 코드 (공통코드 분류 702)';
COMMENT ON COLUMN EMPLOYEE_BANK_ACCOUNT.ACNT_NO      IS '계좌번호 (문자열, 앞자리 0·하이픈 보존)';
COMMENT ON COLUMN EMPLOYEE_BANK_ACCOUNT.DEPOSITOR_NM IS '예금주명';
COMMENT ON COLUMN EMPLOYEE_BANK_ACCOUNT.USE_YN       IS '사용 여부 (Y:사용 / N:미사용)';
COMMENT ON COLUMN EMPLOYEE_BANK_ACCOUNT.RGTR_ID      IS '등록자 ID';
COMMENT ON COLUMN EMPLOYEE_BANK_ACCOUNT.LAST_MDFR_ID IS '최종 수정자 ID';
COMMENT ON COLUMN EMPLOYEE_BANK_ACCOUNT.REG_DT       IS '등록일시';
COMMENT ON COLUMN EMPLOYEE_BANK_ACCOUNT.MDFCN_DT     IS '수정일시';
