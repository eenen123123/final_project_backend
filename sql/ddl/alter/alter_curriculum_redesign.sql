-- =====================================================================
-- 커리큘럼 도메인 재설계 (Option A)
-- 목적: 가이드라인의 Curriculum → Course → Lesson 계층 구조에 맞게 DB 재정의
-- 실행 환경: Oracle
-- 실행 순서: 반드시 아래 순서대로 실행 (FK 의존성 때문)
-- =====================================================================


-- =====================================================================
-- 1. CURRICULUM_DETAIL 제거
--    이유: 주차/주제/내용 표(syllabus outline)는 가이드 도메인과 맞지 않음
--          COURSE 테이블이 이미 CURRICULUM_ID FK를 가지므로 불필요
-- =====================================================================
DROP TABLE CURRICULUM_DETAIL;
DROP SEQUENCE SEQ_CURRICULUM_DETAIL;


-- =====================================================================
-- 2. CURRICULUM_MASTER → CURRICULUM 이름 변경
--    이유: _MASTER 접미사 불필요, 단순화
--    참고: Oracle RENAME 시 COURSE.FK_COURSE_CURRICULUM 자동 유지됨
-- =====================================================================
RENAME CURRICULUM_MASTER TO CURRICULUM;
ALTER TABLE CURRICULUM RENAME CONSTRAINT PK_CURRICULUM_MASTER TO PK_CURRICULUM;


-- =====================================================================
-- 3. COURSE_CONTENT → LECTURE 이름 변경 + 컬럼 정리
--    이유: 가이드의 "강의(1회 수업 단위)" 개념과 정확히 일치
--    참고: 기존 LECTURE 테이블은 이미 COURSE_COHORT로 변경 완료
-- =====================================================================
RENAME COURSE_CONTENT TO LECTURE;

-- PK / FK 제약조건 이름 정리
ALTER TABLE LECTURE RENAME CONSTRAINT PK_COURSE_CONTENT TO PK_LECTURE;
ALTER TABLE LECTURE DROP CONSTRAINT FK_COURSE_TO_COURSE_CONTENT_1;
ALTER TABLE LECTURE ADD CONSTRAINT FK_LECTURE_COURSE
    FOREIGN KEY (COURSE_SN) REFERENCES COURSE(COURSE_SN);

-- 컬럼명 변경
ALTER TABLE LECTURE RENAME COLUMN CONT_SN             TO LECTURE_SN;
ALTER TABLE LECTURE RENAME COLUMN LECT_SJ             TO LECTURE_NM;
ALTER TABLE LECTURE RENAME COLUMN LECT_TYPE_CD        TO LECTURE_TYPE_CD;
ALTER TABLE LECTURE RENAME COLUMN VIDEO_PLAY_TIME_CNT TO LECTURE_DURATION;

-- 신규 컬럼 추가
ALTER TABLE LECTURE ADD LECTURE_EXPLN_CN  VARCHAR2(4000);
ALTER TABLE LECTURE ADD OPNN_YN           CHAR(1) DEFAULT 'Y';
ALTER TABLE LECTURE ADD LOCK_YN           CHAR(1) DEFAULT 'N';
ALTER TABLE LECTURE ADD PREREQ_LECTURE_SN NUMBER(20);
-- PREREQ_LECTURE_SN: PK가 복합키(LECTURE_SN, COURSE_SN)이므로 FK 제약 없이 소프트 참조

-- 코멘트
COMMENT ON TABLE  LECTURE                   IS '강의 테이블 (1회 수업 단위, 구 COURSE_CONTENT)';
COMMENT ON COLUMN LECTURE.LECTURE_SN        IS '기본키(PK) · 시퀀스';
COMMENT ON COLUMN LECTURE.COURSE_SN         IS 'COURSE.COURSE_SN 참조 (FK)';
COMMENT ON COLUMN LECTURE.LECTURE_NM        IS '강의명 (예: 1차시. 오리엔테이션)';
COMMENT ON COLUMN LECTURE.LECTURE_TYPE_CD   IS 'COM_CD 공통코드 참조';
COMMENT ON COLUMN LECTURE.LECTURE_DURATION  IS '강의 소요시간 (초 단위)';
COMMENT ON COLUMN LECTURE.LECTURE_EXPLN_CN  IS '강의 설명';
COMMENT ON COLUMN LECTURE.OPNN_YN           IS 'Y:공개 / N:비공개';
COMMENT ON COLUMN LECTURE.LOCK_YN           IS 'Y:잠금(선수강의 미이수) / N:열림';
COMMENT ON COLUMN LECTURE.PREREQ_LECTURE_SN IS '선수 강의 LECTURE_SN (소프트 자기참조)';
COMMENT ON COLUMN LECTURE.SORT_ORD          IS '강좌 내 강의 노출 순서';


-- =====================================================================
-- 4. LECTURE → COURSE_COHORT 이름 변경 + 컬럼 정리
--    이유: LECTURE는 실제로 강의 기수(운영 회차) 개념
--          CLASSROOM과 역할 분리 명확화를 위해 COURSE_COHORT로 재명명
-- =====================================================================
RENAME LECTURE TO COURSE_COHORT;

-- PK 제약조건 이름 정리
ALTER TABLE COURSE_COHORT RENAME CONSTRAINT PK_LECTURE TO PK_COURSE_COHORT;

-- 컬럼명 변경
ALTER TABLE COURSE_COHORT RENAME COLUMN LECT_SN       TO COHORT_SN;
ALTER TABLE COURSE_COHORT RENAME COLUMN LECT_YEAR     TO COHORT_YEAR;
ALTER TABLE COURSE_COHORT RENAME COLUMN LECT_STAT_CD  TO COHORT_STAT_CD;
ALTER TABLE COURSE_COHORT RENAME COLUMN LECT_STRT_YMD TO COHORT_STRT_YMD;
ALTER TABLE COURSE_COHORT RENAME COLUMN LECT_END_YMD  TO COHORT_END_YMD;
ALTER TABLE COURSE_COHORT RENAME COLUMN LRN_TIME_CNT  TO TOT_LRN_TIME_CNT;

-- 코멘트
COMMENT ON TABLE  COURSE_COHORT                  IS '강좌 기수(운영 회차) 테이블 (구 LECTURE)';
COMMENT ON COLUMN COURSE_COHORT.COHORT_SN        IS '기본키(PK) · 시퀀스';
COMMENT ON COLUMN COURSE_COHORT.COURSE_SN        IS 'COURSE.COURSE_SN 참조 (FK)';
COMMENT ON COLUMN COURSE_COHORT.COHORT_YEAR      IS '운영 연도 (예: 2025)';
COMMENT ON COLUMN COURSE_COHORT.COHORT_STAT_CD   IS '기수 상태 코드 (COM_CD 참조)';
COMMENT ON COLUMN COURSE_COHORT.COHORT_STRT_YMD  IS '기수 시작일 (YYYYMMDD)';
COMMENT ON COLUMN COURSE_COHORT.COHORT_END_YMD   IS '기수 종료일 (YYYYMMDD)';
COMMENT ON COLUMN COURSE_COHORT.TOT_LRN_TIME_CNT IS '총 학습 시간 (HH:MM:SS)';


-- =====================================================================
-- 5. COURSE 컬럼 추가
--    이유: 커리큘럼 내 강좌 순서 관리 + 선수 강좌 조건 지원
-- =====================================================================
ALTER TABLE COURSE ADD SORT_ORD        NUMBER(10) DEFAULT 0;
ALTER TABLE COURSE ADD PREREQ_COURSE_SN NUMBER(20);
ALTER TABLE COURSE ADD CONSTRAINT FK_COURSE_PREREQ
    FOREIGN KEY (PREREQ_COURSE_SN) REFERENCES COURSE(COURSE_SN);

-- 커리큘럼 설계 단계에서 과목 분류는 필수가 아님
ALTER TABLE COURSE MODIFY SUBJ_ID    NULL;
ALTER TABLE COURSE MODIFY SUBJ_CL_ID NULL;

COMMENT ON COLUMN COURSE.SORT_ORD         IS '커리큘럼 내 강좌 노출 순서';
COMMENT ON COLUMN COURSE.PREREQ_COURSE_SN IS '선수 강좌 COURSE_SN (자기참조 FK)';


COMMIT;
