-- =============================================
-- 1. CLASSROOM (CLASS_SN identity 자동 생성)
-- =============================================
-- Java 기초 1기반
insert into classroom (
   opnr_user_id,
   course_sn,
   class_nm,
   enrl_strt_ymd,
   enrl_end_ymd,
   use_yn,
   rgtr_id,
   reg_dt,
   last_mdfr_id,
   mdfcn_dt
) values ( 'testinstructor01',
           2,
           'Java 기초 1기반',
           '20250301',
           '20250831',
           'Y',
           'testinstructor01',
           sysdate,
           'testinstructor01',
           sysdate );

-- Java 기초 2기반
insert into classroom (
   opnr_user_id,
   course_sn,
   class_nm,
   enrl_strt_ymd,
   enrl_end_ymd,
   use_yn,
   rgtr_id,
   reg_dt,
   last_mdfr_id,
   mdfcn_dt
) values ( 'testinstructor01',
           2,
           'Java 기초 2기반',
           '20250401',
           '20250930',
           'Y',
           'testinstructor01',
           sysdate,
           'testinstructor01',
           sysdate );

-- Spring Boot 실전 1기반
insert into classroom (
   opnr_user_id,
   course_sn,
   class_nm,
   enrl_strt_ymd,
   enrl_end_ymd,
   use_yn,
   rgtr_id,
   reg_dt,
   last_mdfr_id,
   mdfcn_dt
) values ( 'testinstructor01',
           3,
           'Spring Boot 실전 1기반',
           '20250501',
           '20251031',
           'Y',
           'testinstructor01',
           sysdate,
           'testinstructor01',
           sysdate );

commit;


-- CLASS_SN 확인 (다음 INSERT에 필요)
select class_sn,
       class_nm
  from classroom
 order by class_sn;

-- =============================================
-- 2. CLASSROOM_MEMBER
--    아래 /*n*/ 자리에 위 SELECT 결과 CLASS_SN 대입
-- =============================================
-- Java 기초 1기반
insert into classroom_member (
   enrl_sn,
   class_sn,
   user_id,
   enrl_stat_cd,
   rgtr_id,
   reg_dt,
   last_mdfr_id,
   mdfcn_dt
) values ( seq_classroom_member.nextval,
           4,
           'testuser10',
           '01',
           'testinstructor01',
           sysdate,
           'testinstructor01',
           sysdate );

insert into classroom_member (
   enrl_sn,
   class_sn,
   user_id,
   enrl_stat_cd,
   rgtr_id,
   reg_dt,
   last_mdfr_id,
   mdfcn_dt
) values ( seq_classroom_member.nextval,
           4,
           'testuser16',
           '01',
           'testinstructor01',
           sysdate,
           'testinstructor01',
           sysdate );

insert into classroom_member (
   enrl_sn,
   class_sn,
   user_id,
   enrl_stat_cd,
   rgtr_id,
   reg_dt,
   last_mdfr_id,
   mdfcn_dt
) values ( seq_classroom_member.nextval,
           4,
           'testuser22',
           '02',
           'testinstructor01',
           sysdate,
           'testinstructor01',
           sysdate );

-- Java 기초 2기반
insert into classroom_member (
   enrl_sn,
   class_sn,
   user_id,
   enrl_stat_cd,
   rgtr_id,
   reg_dt,
   last_mdfr_id,
   mdfcn_dt
) values ( seq_classroom_member.nextval,
           5,
           'testuser28',
           '01',
           'testinstructor01',
           sysdate,
           'testinstructor01',
           sysdate );

insert into classroom_member (
   enrl_sn,
   class_sn,
   user_id,
   enrl_stat_cd,
   rgtr_id,
   reg_dt,
   last_mdfr_id,
   mdfcn_dt
) values ( seq_classroom_member.nextval,
           5,
           'testuser34',
           '01',
           'testinstructor01',
           sysdate,
           'testinstructor01',
           sysdate );

insert into classroom_member (
   enrl_sn,
   class_sn,
   user_id,
   enrl_stat_cd,
   rgtr_id,
   reg_dt,
   last_mdfr_id,
   mdfcn_dt
) values ( seq_classroom_member.nextval,
           5,
           'testuser36',
           '03',
           'testinstructor01',
           sysdate,
           'testinstructor01',
           sysdate );

-- Spring Boot 실전 1기반
insert into classroom_member (
   enrl_sn,
   class_sn,
   user_id,
   enrl_stat_cd,
   rgtr_id,
   reg_dt,
   last_mdfr_id,
   mdfcn_dt
) values ( seq_classroom_member.nextval,
           6,
           'testuser46',
           '01',
           'testinstructor01',
           sysdate,
           'testinstructor01',
           sysdate );

insert into classroom_member (
   enrl_sn,
   class_sn,
   user_id,
   enrl_stat_cd,
   rgtr_id,
   reg_dt,
   last_mdfr_id,
   mdfcn_dt
) values ( seq_classroom_member.nextval,
           6,
           'testuser49',
           '01',
           'testinstructor01',
           sysdate,
           'testinstructor01',
           sysdate );

insert into classroom_member (
   enrl_sn,
   class_sn,
   user_id,
   enrl_stat_cd,
   rgtr_id,
   reg_dt,
   last_mdfr_id,
   mdfcn_dt
) values ( seq_classroom_member.nextval,
           6,
           'testuser51',
           '01',
           'testinstructor01',
           sysdate,
           'testinstructor01',
           sysdate );

commit;