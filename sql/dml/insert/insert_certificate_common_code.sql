-- ================================================================
--  증명서 종류 공통코드 (CL_CODE = '228')
--  · 증명서 발급 화면의 select box 옵션을 공통코드로 관리
-- ================================================================
INSERT INTO COM_CL (CL_CODE, CL_CD_NM, CL_CD_EXPLN, USE_YN, RGTR_ID, REG_DT)
VALUES ('228', '증명서 종류', '직원 대상 증명서 발급 종류 구분', 'Y', 'SYSTEM', SYSTIMESTAMP);

INSERT INTO COM_CD (COM_CD, CL_CODE, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID, REG_DT)
VALUES ('01', '228', '재직증명서', '현재 재직 사실을 증명', 'Y', 'SYSTEM', SYSTIMESTAMP);

INSERT INTO COM_CD (COM_CD, CL_CODE, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID, REG_DT)
VALUES ('02', '228', '경력증명서', '근무 경력을 증명', 'Y', 'SYSTEM', SYSTIMESTAMP);

-- 급여확인서(03)는 실지급 명세가 외부 급여 솔루션에 있어 셀프 발급에서 제외함.
-- 이미 운영 DB에 03이 들어가 있다면 아래로 비활성화(셀프 화면은 USE_YN='Y'만 노출):
UPDATE COM_CD SET USE_YN = 'N' WHERE CL_CODE = '228' AND COM_CD = '03';

COMMIT;
