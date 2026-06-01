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