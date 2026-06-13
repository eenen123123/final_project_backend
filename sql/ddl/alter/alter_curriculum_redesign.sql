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
drop table curriculum_detail;
drop sequence seq_curriculum_detail;


-- =====================================================================
-- 2. CURRICULUM_MASTER → CURRICULUM 이름 변경
--    이유: _MASTER 접미사 불필요, 단순화
--    참고: Oracle RENAME 시 COURSE.FK_COURSE_CURRICULUM 자동 유지됨
-- =====================================================================
rename curriculum_master to curriculum;
alter table curriculum rename constraint pk_curriculum_master to pk_curriculum;


-- =====================================================================
-- 3. COURSE_CONTENT → LECTURE 이름 변경 + 컬럼 정리
--    이유: 가이드의 "강의(1회 수업 단위)" 개념과 정확히 일치
--    참고: 기존 LECTURE 테이블은 이미 COURSE_COHORT로 변경 완료
-- =====================================================================
rename course_content to lecture;

-- PK / FK 제약조건 이름 정리
alter table lecture rename constraint pk_course_content to pk_lecture;
alter table lecture drop constraint fk_course_to_course_content_1;
alter table lecture
   add constraint fk_lecture_course foreign key ( course_sn )
      references course ( course_sn );

-- 컬럼명 변경
alter table lecture rename column cont_sn to lecture_sn;
alter table lecture rename column lect_sj to lecture_nm;
alter table lecture rename column lect_type_cd to lecture_type_cd;
alter table lecture rename column video_play_time_cnt to lecture_duration;

-- 신규 컬럼 추가
alter table lecture add lecture_expln_cn varchar2(4000);
alter table lecture add opnn_yn char(1) default 'Y';
alter table lecture add lock_yn char(1) default 'N';
alter table lecture add prereq_lecture_sn number(20);
-- PREREQ_LECTURE_SN: PK가 복합키(LECTURE_SN, COURSE_SN)이므로 FK 제약 없이 소프트 참조

-- 코멘트
comment on table lecture is
   '강의 테이블 (1회 수업 단위, 구 COURSE_CONTENT)';
comment on column lecture.lecture_sn is
   '기본키(PK) · 시퀀스';
comment on column lecture.course_sn is
   'COURSE.COURSE_SN 참조 (FK)';
comment on column lecture.lecture_nm is
   '강의명 (예: 1차시. 오리엔테이션)';
comment on column lecture.lecture_type_cd is
   'COM_CD 공통코드 참조';
comment on column lecture.lecture_duration is
   '강의 소요시간 (초 단위)';
comment on column lecture.lecture_expln_cn is
   '강의 설명';
comment on column lecture.opnn_yn is
   'Y:공개 / N:비공개';
comment on column lecture.lock_yn is
   'Y:잠금(선수강의 미이수) / N:열림';
comment on column lecture.prereq_lecture_sn is
   '선수 강의 LECTURE_SN (소프트 자기참조)';
comment on column lecture.sort_ord is
   '강좌 내 강의 노출 순서';


-- =====================================================================
-- 4. LECTURE → COURSE_COHORT 이름 변경 + 컬럼 정리
--    이유: LECTURE는 실제로 강의 기수(운영 회차) 개념
--          CLASSROOM과 역할 분리 명확화를 위해 COURSE_COHORT로 재명명
-- =====================================================================
rename lecture to course_cohort;

-- PK 제약조건 이름 정리
alter table course_cohort rename constraint pk_lecture to pk_course_cohort;

-- 컬럼명 변경
alter table course_cohort rename column lect_sn to cohort_sn;
alter table course_cohort rename column lect_year to cohort_year;
alter table course_cohort rename column lect_stat_cd to cohort_stat_cd;
alter table course_cohort rename column lect_strt_ymd to cohort_strt_ymd;
alter table course_cohort rename column lect_end_ymd to cohort_end_ymd;
alter table course_cohort rename column lrn_time_cnt to tot_lrn_time_cnt;

-- 코멘트
comment on table course_cohort is
   '강좌 기수(운영 회차) 테이블 (구 LECTURE)';
comment on column course_cohort.cohort_sn is
   '기본키(PK) · 시퀀스';
comment on column course_cohort.course_sn is
   'COURSE.COURSE_SN 참조 (FK)';
comment on column course_cohort.cohort_year is
   '운영 연도 (예: 2025)';
comment on column course_cohort.cohort_stat_cd is
   '기수 상태 코드 (COM_CD 참조)';
comment on column course_cohort.cohort_strt_ymd is
   '기수 시작일 (YYYYMMDD)';
comment on column course_cohort.cohort_end_ymd is
   '기수 종료일 (YYYYMMDD)';
comment on column course_cohort.tot_lrn_time_cnt is
   '총 학습 시간 (HH:MM:SS)';


-- =====================================================================
-- 5. COURSE 컬럼 추가
--    이유: 커리큘럼 내 강좌 순서 관리 + 선수 강좌 조건 지원
-- =====================================================================
alter table course add sort_ord number(10) default 0;
alter table course add prereq_course_sn number(20);
alter table course
   add constraint fk_course_prereq foreign key ( prereq_course_sn )
      references course ( course_sn );

-- 커리큘럼 설계 단계에서 과목 분류는 필수가 아님
alter table course modify
   subj_id null;
alter table course modify
   subj_cl_id null;

comment on column course.sort_ord is
   '커리큘럼 내 강좌 노출 순서';
comment on column course.prereq_course_sn is
   '선수 강좌 COURSE_SN (자기참조 FK)';


commit;

-- 커리큘럼 테이블에 커리큘럼 설명, 시작일, 종료일 컬럼 추가 (옵션)
alter table curriculum add (
   strt_dt  date,
   end_dt   date,
   expln_cn varchar2(2000)
);