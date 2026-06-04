-- ============================================================
-- INSTRUCTOR_CAREER 테이블 보완 ALTER
-- 프로젝트 표준 이력 컬럼 및 소프트 딜리트 컬럼 추가
--
-- [추가 컬럼]
--   이력 관리 (프로젝트 표준 패턴: RGTR_ID / REG_DT / LAST_MDFR_ID / MDFCN_DT)
--     RGTR_ID      : 최초 등록자 ID
--     REG_DT       : 최초 등록일시 (DEFAULT CURRENT_TIMESTAMP)
--     LAST_MDFR_ID : 최종 수정자 ID
--     MDFCN_DT     : 최종 수정일시
--
--   소프트 딜리트 (프로젝트 표준 패턴: DEL_YN / DEL_DT / DEL_USER_ID)
--     DEL_YN       : 삭제 여부 (Y:삭제 / N:정상, DEFAULT 'N')
--     DEL_DT       : 삭제 처리 일시
--     DEL_USER_ID  : 삭제 처리자 ID
-- ============================================================

-- 이력 컬럼 추가
ALTER TABLE INSTRUCTOR_CAREER ADD (
    RGTR_ID      VARCHAR2(20)  NULL,
    REG_DT       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NULL,
    LAST_MDFR_ID VARCHAR2(20)  NULL,
    MDFCN_DT     TIMESTAMP     NULL
);

-- 소프트 딜리트 컬럼 추가
ALTER TABLE INSTRUCTOR_CAREER ADD (
    DEL_YN      CHAR(1)     DEFAULT 'N' NOT NULL,
    DEL_DT      TIMESTAMP   NULL,
    DEL_USER_ID VARCHAR2(20) NULL
);

-- 기존 데이터 등록자 초기화 (이미 INSERT된 데이터가 있는 경우)
-- 등록자 정보를 알 수 없으므로 INSTR_USER_ID로 대체
UPDATE INSTRUCTOR_CAREER
SET RGTR_ID = INSTR_USER_ID,
    REG_DT  = CURRENT_TIMESTAMP
WHERE RGTR_ID IS NULL;

COMMIT;

-- 컬럼 코멘트
COMMENT ON COLUMN INSTRUCTOR_CAREER.RGTR_ID      IS '최초 등록자 ID';
COMMENT ON COLUMN INSTRUCTOR_CAREER.REG_DT       IS '최초 등록일시';
COMMENT ON COLUMN INSTRUCTOR_CAREER.LAST_MDFR_ID IS '최종 수정자 ID';
COMMENT ON COLUMN INSTRUCTOR_CAREER.MDFCN_DT     IS '최종 수정일시';
COMMENT ON COLUMN INSTRUCTOR_CAREER.DEL_YN       IS '삭제 여부 (Y:삭제 / N:정상)';
COMMENT ON COLUMN INSTRUCTOR_CAREER.DEL_DT       IS '삭제 처리 일시';
COMMENT ON COLUMN INSTRUCTOR_CAREER.DEL_USER_ID  IS '삭제 처리자 ID';
