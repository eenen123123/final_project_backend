-- 1) CLASSROOM 관련 시퀀스 확인
select sequence_name
  from user_sequences
 where sequence_name like '%CLASS%';

-- 2) COURSE 테이블에 데이터 있는지 확인 (COURSE_SN 필요)
select course_sn,
       course_nm,
       instr_user_id
  from course
 where rownum <= 5;

-- 3) MEMBER 중 강사 역할 사용자 확인 (OPNR_USER_ID로 쓸 것)
select user_id,
       user_name
  from member
 where rownum <= 10;

 -- 강사 역할 사용자 조회
select m.user_id,
       m.user_name
  from member m
  join member_role_mapping mrm
on m.user_id = mrm.user_id
  join member_role mr
on mrm.user_role_cd = mr.user_role_cd
 where mr.user_role_cd = 'ROLE_INSTRUCTOR';

 -- 학생 역할 사용자 조회
select m.user_id,
       m.user_name
  from member m
  join member_role_mapping mrm
on m.user_id = mrm.user_id
 where mrm.user_role_cd = 'ROLE_STUDENT'
   and rownum <= 10;