-- ============================================================
-- INSTRUCTOR_JOURNAL 테이블 소프트 딜리트 컬럼 추가
--
-- [추가 컬럼]
--   DEL_YN      : 삭제 여부 (Y:삭제 / N:정상, DEFAULT 'N')
--   DEL_DT      : 삭제 처리 일시
--   DEL_USER_ID : 삭제 처리자 ID
-- ============================================================

ALTER TABLE INSTRUCTOR_JOURNAL ADD (
    DEL_YN      CHAR(1)      DEFAULT 'N' NOT NULL,
    DEL_DT      TIMESTAMP    NULL,
    DEL_USER_ID VARCHAR2(20) NULL
);

COMMIT;

COMMENT ON COLUMN INSTRUCTOR_JOURNAL.DEL_YN      IS '삭제 여부 (Y:삭제 / N:정상)';
COMMENT ON COLUMN INSTRUCTOR_JOURNAL.DEL_DT      IS '삭제 처리 일시';
COMMENT ON COLUMN INSTRUCTOR_JOURNAL.DEL_USER_ID IS '삭제 처리자 ID';
