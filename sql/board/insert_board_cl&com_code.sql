-- COM_CL 공통 코드 사용
-- 통합 게시판 타입 분류 => CL_CODE: 100
-- FAQ 게시판 대분류 => CL_CODE: 101
-- FAQ 게시판 중분류 => CL_CODE: 102

-- 1. COM_CL 분류 등록
INSERT INTO COM_CL (CL_CODE, CL_CD_NM, CL_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('100', '게시판', '통합 게시판 분류', 'Y', 'admin');

INSERT INTO COM_CL (CL_CODE, CL_CD_NM, CL_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('101', 'FAQ대분류', 'FAQ 게시판 대분류', 'Y', 'admin');

INSERT INTO COM_CL (CL_CODE, CL_CD_NM, CL_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('102', 'FAQ중분류', 'FAQ 게시판 중분류', 'Y', 'admin');

-- 2. COM_CD 게시판 종류 (CL_CODE: 100)
INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('100', '01', 'FAQ', 'FAQ 게시판', 'Y', 'admin');

INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('100', '02', 'NOTICE', '공지사항 게시판', 'Y', 'admin');

INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('100', '03', 'QNA', 'QnA 게시판', 'Y', 'admin');

INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('100', '04', 'DATA_ROOM', '자료실', 'Y', 'admin');

-- 3. COM_CD FAQ 대분류 (CL_CODE: 101)
INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('101', '01', '강의/교재', 'FAQ 강의/교재 대분류', 'Y', 'admin');

INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('101', '02', '결제', 'FAQ 결제 대분류', 'Y', 'admin');

INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('101', '03', '학습기기', 'FAQ 학습기기 대분류', 'Y', 'admin');

INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('101', '04', '동영상', 'FAQ 동영상 대분류', 'Y', 'admin');

INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('101', '05', '도서', 'FAQ 도서 대분류', 'Y', 'admin');

-- 4. COM_CD FAQ 중분류 (CL_CODE: 102)
-- 강의/교재
SET DEFINE OFF;
INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('102', '01', '수강신청', '강의/교재 - 수강신청', 'Y', 'admin');

INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('102', '02', '수강기간', '강의/교재 - 수강기간', 'Y', 'admin');

INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('102', '03', '교재', '강의/교재 - 교재', 'Y', 'admin');

INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('102', '04', '환불/취소', '강의/교재 - 환불/취소', 'Y', 'admin');

-- 결제
INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('102', '05', '결제수단', '결제 - 결제수단', 'Y', 'admin');

INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('102', '06', '영수증', '결제 - 영수증', 'Y', 'admin');

INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('102', '07', '쿠폰/포인트', '결제 - 쿠폰/포인트', 'Y', 'admin');

-- 학습기기
INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('102', '08', '기기등록', '학습기기 - 기기등록', 'Y', 'admin');

INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('102', '09', '앱설치', '학습기기 - 앱설치', 'Y', 'admin');

INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('102', '10', '오류', '학습기기 - 오류', 'Y', 'admin');

-- 동영상
INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('102', '11', '플레이어 기능', '동영상 - 플레이어 기능', 'Y', 'admin');

INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('102', '12', '플레이어 설치', '동영상 - 플레이어 설치', 'Y', 'admin');

INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('102', '13', '플레이어 재생', '동영상 - 플레이어 재생', 'Y', 'admin');

-- 도서
INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('102', '14', '주문', '도서 - 주문', 'Y', 'admin');

INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('102', '15', '배송', '도서 - 배송', 'Y', 'admin');

INSERT INTO COM_CD (CL_CODE, COM_CD, COM_CD_NM, COM_CD_EXPLN, USE_YN, RGTR_ID)
VALUES ('102', '16', '반품/교환', '도서 - 반품/교환', 'Y', 'admin');

COMMIT;

-- FAQ 테이블에 중분류 추가

ALTER TABLE FAQ 

ADD FAQ_SUB_CTG_CD CHAR(2) DEFAULT NULL;

COMMENT ON COLUMN FAQ.FAQ_SUB_CTG_CD IS 'COM_CD 공통코드 참조 (CL_CODE: 102)';

COMMIT;

-- FAQ 테이블 최종 구조
-- POST_SN — PK
-- FAQ_CTG_CD — 대분류 (CL_CODE: 101)
-- FAQ_SUB_CTG_CD — 중분류 (CL_CODE: 102) ← 추가
-- EXPS_ORD — 노출순서
-- TOP_FIX_YN — BEST 여부


-- 테스트 데이터 
-- 1. BOARD INSERT (POST_SN 생략하면 자동 채번)
INSERT INTO BOARD (WRTR_USER_ID, POST_SJ, POST_CN, REG_DT, MDFCN_DT)
VALUES ('admin', '[Win] 플레이어 설치방법', '윈도우 플레이어 설치 방법입니다...', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. FAQ INSERT (방금 INSERT된 POST_SN 가져오기)
INSERT INTO FAQ (POST_SN, FAQ_CTG_CD, FAQ_SUB_CTG_CD, EXPS_ORD, TOP_FIX_YN)
VALUES (ISEQ$$_433495.CURRVAL, '04', '12', 1, 'Y');

COMMIT;