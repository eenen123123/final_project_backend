insert into course (
   subj_id,
   subj_cl_id,
   instr_user_id,
   course_nm,
   course_expln_cn,
   opnn_yn,
   prod_mthd_cd,
   course_price,
   rgtr_id,
   reg_dt,
   last_mdfr_id,
   mdfcn_dt
) values ( 1,
           1,
           'testuser04',
           'Python 데이터 분석 기초',
           'Python을 활용한 데이터 분석 입문 강좌입니다.',
           'Y',
           '01',
           0,
           'testuser04',
           sysdate,
           'testuser04',
           sysdate );

commit;

-- 과목 분류 확인
select subj_cl_id,
       subj_cl_nm
  from subject_classification
 where rownum <= 5;

-- 과목 확인
select subj_id,
       subj_nm,
       subj_cl_id
  from subject
 where rownum <= 5;

-- 커리큘럼 확인 (연결할 경우)
select curriculum_id,
       title
  from curriculum_master
 where instructor_id = 'testinstructor01'
   and use_yn = 'Y';

-- FK 제약조건 확인
select uc.constraint_name,
       ucc.column_name,
       uc.r_constraint_name
  from user_constraints uc
  join user_cons_columns ucc
on uc.constraint_name = ucc.constraint_name
 where uc.table_name = 'COURSE'
   and uc.constraint_type = 'R';


   -- COURSE 테스트 데이터 삽입
-- INSTR_USER_ID, CURRICULUM_ID는 실제 존재하는 값으로 교체 필요
insert into course (
   instr_user_id,
   course_nm,
   course_expln_cn,
   tot_lrn_time_cnt,
   opnn_yn,
   course_price,
   sort_ord,
   curriculum_id,
   rgtr_id,
   reg_dt
) values ( 'testuser05',        -- 실제 강사 USER_ID로 교체
           '지수함수와 로그함수',
           '▣ 평가원과 기출을 분석하여 필수 개념만을 녹여낸 수능에 가장 최적화된 강의

▣ 수능수학 교과서개념+실전개념까지 수능에 꼭 필요한 수능개념 완성

▣ 개념에 이은 수능유형분석을 통해 실전 적응력까지 배양

▣ 확실한 개념 완성을 통해 어떤 유형의 문제가 나와도 흔들리지 않는 응용력과 실전능력을 향상시킨다.

▣ 강의 수강 후, 혼자서 때려잡기를 통해 스스로 생각하여 문제를 해결하는 능력을 키울 수 있다.

▣ 5단계 복습을 통해 부족하거나 어려운 개념은 더 철저히 이해할 수 있다.',-- 썸네일 (없으면 NULL)
           '10:30:00',            -- HH:MM:SS 형식
           'Y',                   -- 공개 여부
           250000,
           0,                     -- 커리큘럼 내 노출 순서
           61,                     -- 실제 CURRICULUM_ID로 교체
           'testuser05',        -- 등록자 ID
           current_timestamp );
commit;