-- ANSWER_SUBMIT 테이블 채점 구조 개선
-- 의미불명 공통코드 참조 컬럼 제거, 점수 컬럼으로 대체
-- (GRDG_USER_ID, GRDG_DT 는 채점자/채점일시로 유용하므로 유지)
ALTER TABLE ANSWER_SUBMIT DROP COLUMN GRDG_RSLT_CD;

-- 획득 점수
-- NULL  = 미채점 (서술형/단답형 채점 대기)
-- 0     = 오답 또는 0점
-- 양수  = 획득 점수 (ALLOC_SCR 이하, 부분점수 포함)
-- 세팅 시점:
--   MULTIPLE_CHOICE → 제출 즉시 서버에서 ALLOC_SCR(정답) or 0(오답) 저장
--   SHORT_ANSWER    → 강사 채점 시 0~ALLOC_SCR 저장
--   ESSAY           → 강사 채점 시 0~ALLOC_SCR 저장 (부분점수 포함)
ALTER TABLE ANSWER_SUBMIT ADD SCORE NUMBER(5,2);

COMMENT ON COLUMN ANSWER_SUBMIT.SCORE IS '획득 점수 (NULL=미채점, 0~ALLOC_SCR, 부분점수 포함 가능)';

COMMIT;
