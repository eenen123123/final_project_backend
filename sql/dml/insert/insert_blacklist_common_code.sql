-- ============================================================
-- 주의 학생(블랙리스트) 공통코드 등록 스크립트
--   - COM_CL : 분류(그룹)   700 위험등급 / 701 유형
--   - COM_CD : 코드값
--   * 이벤트 유형(BLKLST_EVT_CD)은 화면 select 가 아니라 서버가 기록하는 값이라
--     공통코드로 등록하지 않고 서버 상수(REG/MOD/ACT/ASGN/REL)로 처리한다.
-- ============================================================

-- ── 분류(COM_CL) ──────────────────────────────────────────
INSERT INTO COM_CL (CL_CODE, CL_CD_NM, CL_CD_EXPLN, USE_YN, RGTR_ID, REG_DT)
VALUES ('700', '주의학생 위험등급', '주의 학생(블랙리스트) 위험 등급 구분', 'Y', 'SYSTEM', SYSTIMESTAMP);

INSERT INTO COM_CL (CL_CODE, CL_CD_NM, CL_CD_EXPLN, USE_YN, RGTR_ID, REG_DT)
VALUES ('701', '주의학생 유형', '주의 학생(블랙리스트) 사유 유형 구분', 'Y', 'SYSTEM', SYSTIMESTAMP);


-- ── 코드값(COM_CD) : 위험등급 (700) ───────────────────────
--   * 해제는 등급이 아니라 STUDENT_BLACK_LIST.BLKLST_END_DT 로 판정하므로
--     등급 코드에는 고위험/관찰만 둔다.
INSERT INTO COM_CD (COM_CD, CL_CODE, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID, REG_DT)
VALUES ('01', '700', '고위험', '즉각 조치 필요', 'Y', 'SYSTEM', SYSTIMESTAMP);

INSERT INTO COM_CD (COM_CD, CL_CODE, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID, REG_DT)
VALUES ('02', '700', '관찰', '지속 모니터링', 'Y', 'SYSTEM', SYSTIMESTAMP);


-- ── 코드값(COM_CD) : 유형 (701) ───────────────────────────
INSERT INTO COM_CD (COM_CD, CL_CODE, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID, REG_DT)
VALUES ('01', '701', '폭력/괴롭힘', '폭력·괴롭힘·수업 방해 등', 'Y', 'SYSTEM', SYSTIMESTAMP);

INSERT INTO COM_CD (COM_CD, CL_CODE, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID, REG_DT)
VALUES ('02', '701', '수납 불이행', '수강료 미납·연체 등', 'Y', 'SYSTEM', SYSTIMESTAMP);

INSERT INTO COM_CD (COM_CD, CL_CODE, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID, REG_DT)
VALUES ('03', '701', '수업 태도', '지각·미참여·수업 태도 불량 등', 'Y', 'SYSTEM', SYSTIMESTAMP);

INSERT INTO COM_CD (COM_CD, CL_CODE, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID, REG_DT)
VALUES ('04', '701', '기타', '기타 사유', 'Y', 'SYSTEM', SYSTIMESTAMP);

INSERT INTO COM_CD (COM_CD, CL_CODE, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID, REG_DT)
VALUES ('05', '701', '욕설/비방', '욕설·비방·모욕성 표현', 'Y', 'SYSTEM', SYSTIMESTAMP);

INSERT INTO COM_CD (COM_CD, CL_CODE, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID, REG_DT)
VALUES ('06', '701', '음란/성희롱', '음란물 게시·성희롱·성적 불쾌감 유발', 'Y', 'SYSTEM', SYSTIMESTAMP);

INSERT INTO COM_CD (COM_CD, CL_CODE, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID, REG_DT)
VALUES ('07', '701', '명예훼손', '허위사실 유포·명예훼손', 'Y', 'SYSTEM', SYSTIMESTAMP);

INSERT INTO COM_CD (COM_CD, CL_CODE, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID, REG_DT)
VALUES ('08', '701', '부정행위', '시험·과제 부정행위(대리·표절 등)', 'Y', 'SYSTEM', SYSTIMESTAMP);

INSERT INTO COM_CD (COM_CD, CL_CODE, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID, REG_DT)
VALUES ('09', '701', '도배/스팸', '도배·광고·스팸성 게시', 'Y', 'SYSTEM', SYSTIMESTAMP);

COMMIT;

